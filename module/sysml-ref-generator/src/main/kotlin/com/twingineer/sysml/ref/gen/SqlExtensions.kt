package com.twingineer.sysml.ref.gen

private val keywords = listOf(
    "constraint",
)

fun String.asSqlEscaped() =
    if (this.isSqlKeyword()) "${this}_" else this

private fun String.isSqlKeyword() =
    keywords.contains(this)