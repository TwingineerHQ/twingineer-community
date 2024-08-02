package com.twingineer.mof.parse

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*

val MofParseNode.id: String
    get() = this["id"].asString()

val MofParseNode.name: String
    get() = this["name"].asString()

val MofParseNode.lowerValue: Int
    get() = try {
        this["lowerValue"]["value"].value
            ?.let { it as? String }
            ?.toInt()
            ?: 0
    } catch (ignored: NullPointerException) {
        0
    }

val MofParseNode.upperValue: Int
    get() = try {
        this["upperValue"]["value"].value
            ?.let { it as? String }
            ?.let {
                return@let when (it) {
                    "*" -> Int.MAX_VALUE
                    else -> it.toInt()
                }
            }
            ?: 1
    } catch (ignored: NullPointerException) {
        1
    }

val MofParseNode.isOrdered: Boolean
    get() = this.has("isOrdered") && this["isOrdered"].asBoolean()

val MofParseNode.isUnique: Boolean
    get() = this.has("isUnique") && this["isUnique"].asBoolean()

fun MofParseNode.asString(): String {
    assert(this.value is String)
    return this.value as String
}

fun MofParseNode.asBoolean(): Boolean =
    when (this.asString()) {
        "true" -> true
        "false" -> false
        else -> throw IllegalArgumentException()
    }

/**
 * Resolve a [JsonNode] object to a primitive value.
 *
 * @param node A [JsonNode] object.
 * @return A primitive value, json object, json array or null.
 */
internal fun resolveInternal(node: JsonNode?): Any? =
    when (node) {
        is BinaryNode -> node.binaryValue()
        is BooleanNode -> node.booleanValue()
        is NullNode -> null
        is BigIntegerNode -> node.bigIntegerValue()
        is DecimalNode -> node.decimalValue()
        is DoubleNode -> node.doubleValue()
        is IntNode -> node.intValue()
        is LongNode -> node.longValue()
        is POJONode -> node.pojo
        is TextNode -> node.textValue()
        is ObjectNode -> toMap(node)
        // container, array or null
        else -> node
    }

/**
 * @param node A json node.
 * @return A map from a json node.
 */
private fun toMap(node: ObjectNode): Map<String, Any> =
    ObjectNodeMap(node)

private data class ObjectNodeMap(
    private val node: ObjectNode
) : AbstractMap<String, Any>() {
    override fun get(key: String): Any? =
        resolveInternal(node[key])

    override val entries: Set<Map.Entry<String, Any>>
        get() {
            val it = node.fields()
            val set: MutableSet<Map.Entry<String, Any>> = LinkedHashSet()
            while (it.hasNext())
                set.add(it.next())
            return set
        }
    override val size: Int
        get() = node.size()
}