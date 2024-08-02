package com.twingineer.sysml.ref.gen

import com.twingineer.mof.parse.*
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

internal fun MofParseScope.attributeAsJavaInterfaceTypeUncounted(attribute: MofParseNode): String {
    val name = attribute.name
    GeneratorConstants.javaTypeOverride.asSequence()
        .find { (regex, _) -> regex.matches(name) }
        ?.value
        ?.let { return it }
    var uncountedType = "Object"
    val typeNode = attribute["type"]
    if (typeNode.value is String) {
        uncountedType =
            typeNode.asString()
                .dereference()
                .name
                .asJavaClassName()
    } else {
        typeNode["href"].asString().let {
            GeneratorConstants.javaTypeMapping.asSequence()
                .find { (regex, _) -> regex.matches(it) }
        }?.let {
            uncountedType = it.value
        }
    }

    if (uncountedType == "Object")
        logger.warn { "[WARNING] Unknown interface type \"$typeNode\" for $name. Defaulting to Object." }

    return uncountedType
}

internal fun MofParseScope.attributeAsJavaInterfaceTypeCounted(attribute: MofParseNode): String {
    val uncountedType = attributeAsJavaInterfaceTypeUncounted(attribute)
    return if (attribute.upperValue > 1) {
        val collectionType =
            when {
                attribute.isUnique -> "Set"
                attribute.isOrdered -> "List"
                else -> "Collection"
            }
        "$collectionType<? extends $uncountedType>"
    } else uncountedType
}

internal fun MofParseScope.attributeAsJavaImplTypeCounted(attribute: MofParseNode): String {
    val uncountedType = attributeAsJavaInterfaceTypeUncounted(attribute)
    return if (attribute.upperValue > 1) {
        val collectionType =
            when {
                attribute.isUnique -> "Set"
                attribute.isOrdered -> "List"
                else -> "Collection"
            }
        "$collectionType<$uncountedType>"
    } else uncountedType
}

internal fun MofParseScope.attributeAsJsonLdType(attribute: MofParseNode): String {
    val name = attribute.name
    GeneratorConstants.jsonLdTypeOverride.asSequence()
        .find { (regex, _) -> regex.matches(name) }
        ?.value
        ?.let { return it }
    var type: String? = null
    val typeNode = attribute["type"]
    if (typeNode.value is String) {
        val typeType = typeNode.asString().dereference()
        type = when (typeType["type"].asString()) {
            "uml:Class" -> "@id"
            "uml:Enumeration" -> "@vocab"
            else -> throw IllegalArgumentException()
        }
    } else {
        typeNode["href"].asString().let {
            GeneratorConstants.jsonLdTypeMapping.asSequence()
                .find { (regex, _) -> regex.matches(it) }
        }?.let {
            type = it.value
        }
    }

    if (type == null)
        logger.error { "[WARNING] Unknown interface type \"$typeNode\" for $name." }

    return type!!
}