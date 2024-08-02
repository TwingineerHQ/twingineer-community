@file:JvmName("GenerateJsonLd")

package com.twingineer.sysml.ref.gen

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import com.twingineer.ktemplar.appendTemplate
import com.twingineer.ktemplar.json
import com.twingineer.mof.parse.MofParseNode
import com.twingineer.mof.parse.asString
import com.twingineer.mof.parse.name
import com.twingineer.mof.parse.parseMof
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writer
import com.twingineer.sysml.ref.gen.GeneratorConstants as const

fun generateJsonLd(inputFile: Path, outputDir: Path) {
    parseMof(inputFile) {
        root["packagedElement"].elements()
            .filter { it["type"].asString() == "uml:Class" }
            .forEach { clazz ->
                val className = clazz.name.asJavaClassName()
                outputDir.resolve("$className.jsonld").writer().use { out ->
                    out.appendTemplate {
                        json(
                            """
{
|   "@context": {
|       "@vocab": "${const.jsonLdBaseIri}",
|       "${const.jsonLdVocabPrefix}": "${const.jsonLdBaseIri}",
|       "dcterms": "http://purl.org/dc/terms/",
|       "xsd": "http://www.w3.org/2001/XMLSchema#"
"""
                        )
                        clazz.attributesRecursivelyUnshadowed()
                            .sortedBy(MofParseNode::name)
                            .forEach { attribute ->
                                json(
                                    """
,
|       "${attribute.name}": {"@type": "${attributeAsJsonLdType(attribute)}"}
"""
                                )
                            }
                        json(
                            """

|   }
}
"""
                        )
                    }
                }
            }
    }
}

class JsonLdGeneratorCli : CliktCommand() {
    private val input: Path by option()
        .path(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)
        .required()
    private val output: Path by option()
        .path(mustExist = false, canBeFile = false, canBeDir = true)
        .required()

    override fun run() {
        Files.createDirectories(output)
        generateJsonLd(input, output)
    }
}

fun main(args: Array<String>) = JsonLdGeneratorCli().main(args)