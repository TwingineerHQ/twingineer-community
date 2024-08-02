package com.twingineer.sysml.ref.gen

import org.apache.commons.lang3.StringUtils
import org.apache.commons.text.WordUtils
import java.nio.charset.StandardCharsets

private val RESERVED_KEYWORDS = setOf(
    "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
    "default", "do", "double", "else", "enum", "extends", "false", "final", "finally", "float", "for", "goto",
    "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "null", "package",
    "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized",
    "this", "throw", "throws", "transient", "true", "try", "void", "volatile", "while",
)
private const val ESCAPE_CHARACTER = '$'

fun String.asJavaClassName(): String =
    StringUtils.capitalize(this).asJavaEscaped()

fun String.asJavaEnumLiteralName(): String =
    this.replace("(.)(\\p{Upper})".toRegex(), "$1_$2").uppercase().asJavaEscaped()

internal fun String.asJavaVariableName(): String =
    StringUtils.uncapitalize(this).asJavaEscaped()

internal fun String.asJavaEscaped(): String {
    if (this.isEmpty())
        return this
    val builder = StringBuilder(this.length)
    val bytes = this.toByteArray(StandardCharsets.UTF_8)
    var c = Char(bytes[0].toUShort())
    if (!Character.isJavaIdentifierStart(c))
        builder.append(ESCAPE_CHARACTER)
    builder.append(c)
    for (i in 1 until bytes.size) {
        c = Char(bytes[i].toUShort())
        builder.append(if (Character.isJavaIdentifierPart(c)) c else ESCAPE_CHARACTER)
    }
    val escaped = builder.toString()
    return if (RESERVED_KEYWORDS.contains(escaped)) escaped + ESCAPE_CHARACTER else escaped
}

internal fun String.asCamelCase(capitalizeFirst: Boolean = false): String =
    WordUtils.capitalize(this, ' ', ':', '_', '-').replace("[ :_-]", "").let {
        if (!capitalizeFirst) WordUtils.uncapitalize(it)
        else it
    }