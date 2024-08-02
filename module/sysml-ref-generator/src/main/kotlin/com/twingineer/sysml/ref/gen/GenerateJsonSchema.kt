@file:JvmName("GenerateJsonSchema")

package com.twingineer.sysml.ref.gen

import com.fasterxml.jackson.core.PrettyPrinter
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.core.util.Separators
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import com.twingineer.ktemplar.*
import com.twingineer.mof.parse.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writer

fun generateJsonSchema(inputFile: Path, outputDir: Path, mapper: ObjectMapper = ObjectMapper()) {
    val pp = DefaultPrettyPrinter()
        .withSeparators(
            PrettyPrinter.DEFAULT_SEPARATORS
                .withObjectFieldValueSpacing(Separators.Spacing.AFTER)
        )
        .apply { indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE) }
    val schemasFile = outputDir.resolve("schemas.json")
    schemasFile.writer().use { out ->
        parseMof(inputFile) {
            out.appendTemplate {
                json(
                    """
{
  "${"$"}schema": "https://json-schema.org/draft/2020-12/schema",
  "${"$"}defs": {
"""
                )
                val baseUri = root["URI"].asString()
                this@parseMof.root["packagedElement"].elements()
                    .sortedBy(MofParseNode::name)
                    .forEach { packagedElement ->
                        val name = packagedElement.name.asJavaClassName()
                        when (packagedElement["type"].value) {
                            "uml:Class" -> {
                                json(
                                    """

|   "$name": {
|     "${"$"}id": "$baseUri/$name",
|     "title": "$name",

"""
                                )
                                val specials = packagedElement.specials().toList()
                                val typeIndentSize: Int
                                if (specials.isNotEmpty()) {
                                    json(
                                        """
|     "anyOf": [
|       {

"""
                                    )
                                    typeIndentSize = 4
                                } else {
                                    typeIndentSize = 0
                                }
                                (fun TemplateScope.() {
                                    json(
                                        """
|     "type": "object",
|     "properties": {
|       "@id": {
|         "type": "string",
|         "format": "uuid"
|       },
|       "@type": {
|         "type": "string",
|         "const": "$name"
|       }
"""
                                    )
                                    val attributes = packagedElement.attributesRecursivelyUnshadowed()
                                        .sortedBy(MofParseNode::name)
                                        .toList()
                                    attributes.forEach { attribute ->
                                        json(
                                            """
                            ,
                            |       "${attribute.name.asJavaVariableName()}": {
                            
                            """
                                        )
                                        val attributeJson = typeJsonSchemaOf(attribute, baseUri, mapper)
                                            .let {
                                                mapper.writer()
                                                    .with(pp)
                                                    .writeValueAsString(it)
                                            }
                                            .drop(1)
                                        (fun TemplateScope.() {
                                            raw(attributeJson)
                                        })
                                            .indent(8)
                                            .invoke(this)
                                    }
                                    json(
                                        """

|     },
|     "required": [
|       "@id",
|       "@type"
"""
                                    )
                                    attributes.forEach { attribute ->
                                        json(
                                            """
,
|       "${attribute.name.asJavaVariableName()}"
"""
                                        )
                                    }
                                    json(
                                        """

|     ],
|     "additionalProperties": false
|   }
"""
                                    )
                                }
                                    .indent(typeIndentSize)
                                    .invoke(this))
                                specials.forEach {
                                    json(
                                        """
,
|       {
|         "${"$"}ref": "$baseUri/${it.name.asJavaClassName()}"
|       }
"""
                                    )
                                }
                                if (specials.isNotEmpty()) {
                                    json(
                                        """

|     ]
|   },
"""
                                    )
                                } else {
                                    json(
                                        """
,
"""
                                    )
                                }
                            }

                            "uml:Enumeration" -> {
                                json(
                                    """

|   "$name": {
|     "${"$"}id": "$baseUri/$name",
|     "title": "$name",
|     "type": "string",
|     "enum": [
"""
                                )
                                packagedElement["ownedLiteral"].elements().forEachIndexed { index, literal ->
                                    if (index != 0)
                                        json(",")
                                    json(
                                        """

|       "${literal.name}"
"""
                                    )
                                }
                                json(
                                    """

|     ]
|   },
"""
                                )
                            }
                        }
                    }
                json(
                    """

|   "Identified": {
|     "${"$"}id": "$baseUri/Identified",
|     "title": "Identified",
|     "type": "object",
|     "properties": {
|       "@id": {
|         "type": "string",
|         "format": "uuid"
|       }
|     },
|     "required": [
|       "@id"
|     ],
|     "additionalProperties": false
|   }
| }
}
"""
                )
            }
        }
    }

    val schemaBundleNode = mapper.readTree(schemasFile.toFile())
    val schema = schemaBundleNode.path("\$schema").asText(null)

    val defsNodes = schemaBundleNode.path("\$defs")
    val defs: MutableMap<String, ObjectNode> = HashMap(defsNodes.size())
    val titles: MutableMap<String, String> = HashMap(defsNodes.size())
    val defsNodesIterator = defsNodes.fields()
    while (defsNodesIterator.hasNext()) {
        val (key, value) = defsNodesIterator.next()
        val def = value as ObjectNode
        val id = def.path("\$id").asText()
        val title = def.path("title").asText()
        assert(key == title)
        titles[id] = title
        defs[id] = def
    }

    for ((id, value) in defs) {
        var copy = value.deepCopy()
        if (schema != null) {
            val def = mapper.createObjectNode()
            def.put("\$schema", schema)
            def.setAll<JsonNode>(copy)
            copy = def
        }
        val refs = refsRecursively(copy, defs)
        if (refs.isNotEmpty()) {
            val defsNode = mapper.createObjectNode()
            for (ref in refs) {
                val defNode = defs[ref]
                defsNode.set<JsonNode>(defNode!!.path("title").asText(ref), defNode)
            }
            copy.set<JsonNode>("\$defs", defsNode)
        }
        val schemaFile = outputDir.resolve("${titles[id]!!.asJavaClassName()}.json")
        mapper.writer()
            .with(pp)
            .writeValue(schemaFile.toFile(), copy)
    }
}

private fun refsRecursively(node: JsonNode, defs: Map<String, ObjectNode>): Set<String> {
    val refs: MutableSet<String> = LinkedHashSet()
    walkRecursively(node).forEach {
        refsRecursively(it, refs, defs)
    }
    return refs
}

private fun refsRecursively(
    node: JsonNode,
    collected: MutableSet<String>,
    defs: Map<String, ObjectNode>
) {
    val ref = node.path("\$ref").asText(null)
    if (ref != null && collected.add(ref)) {
        walkRecursively(defs[ref]!!).forEach {
            refsRecursively(it, collected, defs)
        }
    }
}

private fun walkRecursively(node: JsonNode): Sequence<JsonNode> =
    sequenceOf(node) + node.elements().asSequence().flatMap(::walkRecursively)

private fun MofParseScope.typeJsonSchemaOf(
    attribute: MofParseNode,
    baseUri: String? = null,
    mapper: ObjectMapper = ObjectMapper(),
): JsonNode {
    val resolvedBaseUri = baseUri ?: root["URI"].asString()
    val typeNode = attribute["type"]
    val typeNodeValue = typeNode.value
    val uncountedType =
        if (typeNodeValue is String) {
            val typeReferenced = typeNodeValue.dereference()
            when (typeReferenced["type"].asString()) {
                "uml:Enumeration" -> mapper.createObjectNode()
                    .put("\$ref", "$resolvedBaseUri/${typeReferenced.name.asJavaClassName()}")

                "uml:Class" -> mapper.createObjectNode()
                    .put("\$ref", "$resolvedBaseUri/Identified")
                    .put("\$comment", "$resolvedBaseUri/${typeReferenced.name.asJavaClassName()}")

                else -> throw IllegalArgumentException()
            }
        } else {
            val href: String = typeNode["href"].asString()
            val type: String =
                if (href.endsWith("Boolean")) "boolean"
                else if (href.endsWith("String")) "string"
                else if (href.endsWith("Integer") /* || href.endsWith("UnlimitedNatural")*/) "integer"
                else if (href.endsWith("Real") /* || href.endsWith("double")*/) "number"
                else throw IllegalArgumentException(href)
            mapper.createObjectNode().put("type", type)
        }

    val countedType: JsonNode
    val lowerValue = attribute.lowerValue
    val upperValue = attribute.upperValue
    if (upperValue == 1) {
        countedType = if (lowerValue == 1) {
            uncountedType
        } else {
            mapper.createObjectNode()
                .set(
                    "oneOf",
                    mapper.createArrayNode()
                        .add(uncountedType)
                        .add(mapper.createObjectNode().put("type", "null"))
                )
        }
    } else {
        countedType = mapper.createObjectNode()
            .put("type", "array")
            .set("items", uncountedType)
        if (lowerValue > 0) {
            (countedType as ObjectNode).put("minItems", lowerValue)
        }
        if (upperValue != Int.MAX_VALUE) {
            (countedType as ObjectNode).put("maxItems", upperValue)
        }
    }
    return countedType
}

class JsonSchemaGeneratorCli : CliktCommand() {
    private val input: Path by option()
        .path(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)
        .required()
    private val output: Path by option()
        .path(mustExist = false, canBeFile = false, canBeDir = true)
        .required()

    override fun run() {
        Files.createDirectories(output)
        generateJsonSchema(input, output)
    }
}

fun main(args: Array<String>) = JsonSchemaGeneratorCli().main(args)