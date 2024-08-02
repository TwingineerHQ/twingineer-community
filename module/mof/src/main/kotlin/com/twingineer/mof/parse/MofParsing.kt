package com.twingineer.mof.parse

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.MissingNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import java.nio.file.Path
import java.util.*

fun parseMof(file: Path, block: MofParseScope.() -> Unit) {
    XmlMapper().readTree(file.toFile())
        .let(::JsonMofParseNode)
        .let(::MofParseScope)
        .let(block)
}

class MofParseScope internal constructor(
    val root: MofParseNode,
) {
    private val paths = mutableMapOf<String, LinkedList<MofParseNode>>()

    fun String.dereference(): MofParseNode =
        path(this, root)
            ?.last ?: throw NoSuchElementException()

    val MofParseNode.parent: MofParseNode?
        get() {
            val path = path(this.id, root)
            return if (path != null && path.size > 1) path[path.size - 2]
            else null
        }

    val MofParseNode.domain: MofParseNode?
        get() {
            val id = this.id
            val path = path(id, root)
            val parent =
                if (path != null && path.size > 1) path[path.size - 2]
                else return null

            return when (val parentType = parent["type"].value as String) {
                "uml:Class" -> parent
                "uml:Association" -> {
                    val memberEnds = ids(parent["memberEnd"].value as String)
                    assert(memberEnds.size == 2)
                    val oppositeId = memberEnds.find { it != id }!!
                    val opposite = path(oppositeId, root)!!.last
                    val oppositeType = opposite["type"].value as String
                    path(oppositeType, root)!!.last
                }

                else -> throw IllegalArgumentException("Unknown parent type: $parentType")
            }
        }

    fun MofParseNode.generals(): Sequence<MofParseNode> {
        if (!this.has("generalization"))
            return emptySequence()
        val generalization = this["generalization"]
        return generalization.elements()
            .map { n -> n["general"].value as String }
            .map { id -> id.dereference() }
    }

    fun MofParseNode.generalsRecursively(): Sequence<MofParseNode> {
        val attributables: MutableMap<MofParseNode, MutableSet<MofParseNode>> = LinkedHashMap()
        walkGeneralsRecursively(this, attributables)
        return attributables.keys.asSequence()
    }

    fun MofParseNode.specials(): Sequence<MofParseNode> {
        val id = this.id
        return root["packagedElement"]
            .elements()
            .filter { "uml:Class" == it["type"].asString() }
            .filter { clazz ->
                if (!clazz.has("generalization"))
                    return@filter false
                clazz["generalization"]
                    .elements()
                    .any { id == it["general"].asString() }
            }
    }

    fun MofParseNode.specialsRecursively(): Sequence<MofParseNode> {
        val attributables: MutableMap<MofParseNode, MutableSet<MofParseNode>> = LinkedHashMap()
        walkSpecialsRecursively(this, attributables)
        return attributables.keys.asSequence()
    }

    fun MofParseNode.attributes(): Sequence<MofParseNode> {
        if (!this.has("ownedAttribute"))
            return emptySequence()
        return this["ownedAttribute"].elements()
    }

    fun MofParseNode.redefinedProperty(): Sequence<String> {
        if (!this.has("redefinedProperty"))
            return emptySequence()
        val redefinedPropertyValue = this["redefinedProperty"].value
        if (redefinedPropertyValue !is String)
            return emptySequence()

        return redefinedPropertyValue
            .split(" ".toRegex())
            .dropLastWhile(String::isEmpty)
            .asSequence()
    }

    fun MofParseNode.attributesRecursively(): Sequence<MofParseNode> {
        val attributables: MutableMap<MofParseNode, MutableSet<MofParseNode>> = LinkedHashMap()
        attributables[this] = this.attributes().toMutableSet()
        walkGeneralsRecursively(this, attributables)

        val attributes: MutableMap<String, MofParseNode> = LinkedHashMap()
        attributables.keys
            .asSequence()
            .flatMap { it.attributes() }
            .forEach { attribute ->
                attribute.redefinedProperty()
                    .filter { attributes.containsKey(it) && attributes[it]!!.name == attribute.name }
                    .forEach(attributes::remove)
                attributes[attribute.id] = attribute
            }

        return attributes.values.asSequence()
    }

    fun MofParseNode.attributesRecursivelyUnshadowed(): Sequence<MofParseNode> {
        val attributesRecursively = this.attributesRecursively().toList()
        return attributesRecursively
            .asSequence()
            .withIndex()
            .filter { (index, attribute) ->
                // shadowed
                attributesRecursively.withIndex()
                    .none { (i, it) -> i < index && it.name == attribute.name }
            }
            .map(IndexedValue<MofParseNode>::value)
    }

    private fun walkGeneralsRecursively(
        node: MofParseNode,
        map: MutableMap<MofParseNode, MutableSet<MofParseNode>>,
    ) {
        node.generals()
            .forEach {
                map.getOrPut(it) { LinkedHashSet() }
                    .add(node)
                walkGeneralsRecursively(it, map)
            }
    }

    private fun MofParseNode.walkSpecialsRecursively(
        node: MofParseNode,
        map: MutableMap<MofParseNode, MutableSet<MofParseNode>>,
    ) {
        node.specials()
            .forEach {
                map.getOrPut(it) { LinkedHashSet() }
                    .add(node)
                walkSpecialsRecursively(it, map)
            }
    }

    private fun path(id: String, node: MofParseNode): LinkedList<MofParseNode>? {
        val cached = paths[id]
        if (cached != null) return cached
        val path = LinkedList<MofParseNode>()
        path.add(node)
        val computed = path(id, path)
        if (computed != null) paths[id] = computed
        return computed
    }

    private fun path(id: String, path: LinkedList<MofParseNode>): LinkedList<MofParseNode>? {
        val tail = path.last.json
        val idNode = tail.path("id")
        if (idNode.isTextual && id == idNode.asText()) return path
        nodes(tail)
            .forEach { node ->
                val newPath = LinkedList(path)
                newPath.add(JsonMofParseNode(node))
                val found = path(id, newPath)
                if (found != null) return found
            }
        return null
    }

    private fun nodes(root: JsonNode): Sequence<JsonNode> {
        var nodes = emptySequence<JsonNode>()
        val elements = root.elements()
        while (elements.hasNext()) {
            val element = elements.next()
            if (element.isObject) {
                nodes += sequenceOf(element as ObjectNode)
            }
            if (element.isArray) {
                nodes += elements(element)
            }
        }
        return nodes
    }

    private fun elements(node: JsonNode): Sequence<JsonNode> =
        when (node) {
            is ObjectNode -> sequenceOf(node)
            is ArrayNode -> node.elements().asSequence()
            is MissingNode -> emptySequence()
            else -> throw IllegalArgumentException()
        }

    private fun ids(joined: String): List<String> =
        listOf(*joined.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
}

sealed interface MofParseNode {
    val value: Any?

    fun has(fieldName: String): Boolean

    operator fun get(fieldName: String): MofParseNode

    fun elements(): Sequence<MofParseNode>
}

internal val MofParseNode.json
    get() = (this as JsonMofParseNode).json

@JvmInline
internal value class JsonMofParseNode(
    val json: JsonNode,
) : MofParseNode {
    override val value: Any?
        get() = resolveInternal(json)

    override fun has(fieldName: String): Boolean =
        json.has(fieldName)

    override fun get(fieldName: String): MofParseNode =
        json.get(fieldName)!!
            .let(::JsonMofParseNode)

    override fun elements(): Sequence<MofParseNode> =
        when (json) {
            is ObjectNode -> sequenceOf(this)
            is ArrayNode -> json.elements()
                .asSequence()
                .map(::JsonMofParseNode)

            is MissingNode -> emptySequence()
            else -> throw IllegalArgumentException()
        }
}