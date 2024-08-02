package com.twingineer.sysml.ref.gen

object GeneratorConstants {
    const val javaInterfacePackage = "org.omg.sysml.metamodel"
    const val javaImplPackage = "org.omg.sysml.metamodel.impl"
    const val javaJacksonPackage = "jackson"
    const val identityAttribute = "elementId"
    const val javaIdentityType = "java.util.UUID"
    val javaTypeOverride = mapOf(
        "^$identityAttribute$".toRegex() to javaIdentityType,
    )
    const val jsonLdBaseIri = "http://omg.org/ns/sysml/v2/metamodel#"
    const val jsonLdVocabPrefix = "sysml"
    val jsonLdTypeOverride = mapOf(
        "^$identityAttribute$".toRegex() to "dcterms:identifier",
    )
    val javaTypeMapping: Map<Regex, String> = mutableMapOf(
        ".*Boolean.*".toRegex() to "Boolean",
        ".*String.*".toRegex() to "String",
        ".*Integer.*".toRegex() to "Integer",
        ".*UnlimitedNatural.*".toRegex() to "Integer",
        ".*Real.*".toRegex() to "Double",
        ".*double.*".toRegex() to "Double",
    )
    val jsonLdTypeMapping: Map<Regex, String> = mutableMapOf(
        ".*Boolean.*".toRegex() to "xsd:boolean",
        ".*String.*".toRegex() to "xsd:string",
        ".*Integer.*".toRegex() to "xsd:integer",
        ".*UnlimitedNatural.*".toRegex() to "xsd:nonNegativeInteger",
        ".*Real.*".toRegex() to "xsd:decimal",
        ".*double.*".toRegex() to "xsd:decimal",
    )
}