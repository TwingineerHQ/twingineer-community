@file:JvmName("GenerateImpl")

package com.twingineer.sysml.ref.gen

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import com.twingineer.ktemplar.appendTemplate
import com.twingineer.mof.parse.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writer
import com.twingineer.ktemplar.raw as java
import com.twingineer.sysml.ref.gen.GeneratorConstants as const

fun generateImpl(inputFile: Path, outputDir: Path) {
    parseMof(inputFile) {
        root["packagedElement"].elements()
            .filter { it["type"].asString() == "uml:Class" }
            .forEach { clazz ->
                val name = clazz.name.asJavaClassName()
                outputDir.resolve("${name}Impl.java").writer().use { out ->
                    val packagedElementClassName = clazz.name.asJavaClassName()
                    out.appendTemplate {
                        java(
                            """
// THIS IS AN AUTOGENERATED FILE. DO NOT EDIT THIS FILE DIRECTLY.
package ${const.javaImplPackage};

import ${const.javaInterfacePackage}.*;
import ${const.javaInterfacePackage}.Package;
import ${const.javaInterfacePackage}.Class;

import ${const.javaJacksonPackage}.DataSerializer;
import ${const.javaJacksonPackage}.DataDeserializer;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.hibernate.annotations.Any;
import org.hibernate.annotations.ManyToAny;
import org.hibernate.annotations.FetchMode;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.EnumType;
import javax.persistence.ElementCollection;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.FetchType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Table;
import javax.persistence.SecondaryTable;
import javax.persistence.CollectionTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity(name = "${packagedElementClassName}Impl")
@SecondaryTable(name = "${packagedElementClassName.asSqlEscaped()}")
@org.hibernate.annotations.Table(appliesTo = "${packagedElementClassName.asSqlEscaped()}", fetch = FetchMode.SELECT, optional = false)
@DiscriminatorValue(value = "${packagedElementClassName.asSqlEscaped()}")
@JsonTypeName(value = "${packagedElementClassName.asSqlEscaped()}")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
public${if (clazz.has("isAbstract") && clazz["isAbstract"].asBoolean()) " abstract" else ""} class ${packagedElementClassName}Impl extends SysMLTypeImpl implements $packagedElementClassName {
"""
                        )

                        val attributesRecursively = clazz.attributesRecursively()
                            .sortedBy(MofParseNode::name)
                            .toList()
                        attributesRecursively.forEachIndexed { index, attribute ->
                            val javaInterfaceTypeUncounted = attributeAsJavaInterfaceTypeUncounted(attribute)
                            val javaImplTypeCounted = attributeAsJavaImplTypeCounted(attribute)
                            val variableName = attribute.name.asJavaVariableName()
                            val shadowedBy = attributesRecursively.withIndex()
                                .find { (i, it) -> i < index && it.name == attribute.name }
                                ?.value

                            val isClass: Boolean
                            val isEnumeration: Boolean
                            when (val attributeTypeId = attribute["type"].value) {
                                is String -> {
                                    val attributeType = attributeTypeId.dereference()
                                    val attributeTypeType = attributeType["type"].asString()
                                    isClass = attributeTypeType == "uml:Class"
                                    isEnumeration = !isClass && (attributeTypeType == "uml:Enumeration")
                                }

                                else -> {
                                    isClass = false
                                    isEnumeration = false
                                }
                            }

                            val isDerived = attribute.has("isDerived") && attribute["isDerived"].asBoolean()
                            val isString = javaInterfaceTypeUncounted == "String"
                            val upperValue = attribute.upperValue

                            java(
                                """


                                        """
                            )

                            if (shadowedBy != null) {
                                java(
                                    """

|   /*
|    * shadowed by `${shadowedBy.id}`

"""
                                )
                            }
                            java(
                                """

|   private $javaImplTypeCounted $variableName;

|   @Override
|   @JsonGetter
"""
                            )
                            if (isClass) {
                                java(
                                    """

|   @JsonSerialize(${if (upperValue > 1) "contentUsing" else "using"} = DataSerializer.class)
"""
                                )
                            }
                            if (isString) {
                                java(
                                    """

|   @Lob
|   @org.hibernate.annotations.Type(type = "org.hibernate.type.TextType")
"""
                                )
                            }
                            if (isEnumeration) {
                                java(
                                    """

|   @javax.persistence.Enumerated(EnumType.STRING)
"""
                                )
                            }
                            if (isDerived) {
                                java(
                                    """

|   // @javax.persistence.Transient
"""
                                )
                            }
                            when {
                                upperValue > 1 -> {
                                    when (isClass) {
                                        true -> java(
                                            """

|   @ManyToAny(metaDef = "${javaInterfaceTypeUncounted}MetaDef", metaColumn = @javax.persistence.Column(name = "attribute_type"), fetch = FetchType.LAZY)
|   @JoinTable(name = "${packagedElementClassName}_$variableName", joinColumns = @JoinColumn(name = "class_id"), inverseJoinColumns = @JoinColumn(name = "attribute_id"))
"""
                                        )

                                        false -> java(
                                            """

|   @ElementCollection${if (isString) "(targetClass = String.class)" else ""}
|   @CollectionTable(name = "${packagedElementClassName}_$variableName", joinColumns = @JoinColumn(name = "${packagedElementClassName}_id"))
"""
                                        )
                                    }
                                }

                                else -> {
                                    when (isClass) {
                                        true -> java(
                                            """

|   @Any(metaDef = "${javaInterfaceTypeUncounted}MetaDef", metaColumn = @javax.persistence.Column(name = "${variableName}_type"), fetch = FetchType.LAZY)
|   @JoinColumn(name = "${variableName}_id", table = "${packagedElementClassName.asSqlEscaped()}")
"""
                                        )

                                        false -> java(
                                            """

|   @javax.persistence.Column(name = "$variableName", table = "${packagedElementClassName.asSqlEscaped()}")
"""
                                        )
                                    }
                                }
                            }
                            java(
                                """

|   public $javaImplTypeCounted get${variableName.replaceFirstChar(Char::uppercaseChar)}() {
"""
                            )
                            if (upperValue > 1) {
                                val collectionImplType = when {
                                    attribute.isOrdered -> "ArrayList"
                                    attribute.isUnique -> "HashSet"
                                    else -> "ArrayList"
                                }
                                java(
                                    """

|       if ($variableName == null) {
|           $variableName = new $collectionImplType<>();
|       }
"""
                                )
                            }
                            java(
                                """

|       return $variableName;
|   }
"""
                            )

                            java(
                                """


|   @JsonSetter
"""
                            )
                            if (isClass) {
                                java(
                                    """

|   @JsonDeserialize(${if (upperValue > 1) "contentUsing" else "using"} = DataDeserializer.class, ${if (upperValue > 1) "contentAs" else "as"} = ${javaInterfaceTypeUncounted}Impl.class)
"""
                                )
                            }
                            java(
                                """

|   public void set${variableName.replaceFirstChar(Char::uppercaseChar)}($javaImplTypeCounted $variableName) {
|       this.$variableName = $variableName;
|   }
"""
                            )
                            if (shadowedBy != null) {
                                java(
                                    """


|   */
"""
                                )
                            }
                        }

                        java(
                            """

}
"""
                        )
                    }
                }
            }
        outputDir.resolve("package-info.java").writer().use { out ->
            val classes = root["packagedElement"].elements()
                .filter { it["type"].asString() == "uml:Class" }
                .sortedBy(MofParseNode::name)
                .toList()
            out.appendTemplate {
                java(
                    """
@AnyMetaDefs(value = {
        @AnyMetaDef(name = "SysMLTypeMetaDef", metaType = "string", idType = "${const.javaIdentityType}", metaValues = {
"""
                )
                classes.forEach { clazz ->
                    val className = clazz.name.asJavaClassName()
                    java(
                        """

|               @MetaValue(value = "$className", targetEntity = ${className}Impl.class),
"""
                    )
                }
                java(
                    """

|       }),
"""
                )
                classes.forEach { clazz ->
                    val className = clazz.name.asJavaClassName()
                    java(
                        """

|       @AnyMetaDef(name = "${className}MetaDef", metaType = "string", idType = "${const.javaIdentityType}", metaValues = {
"""
                    )
                    (clazz.specialsRecursively() + clazz)
                        .sortedBy(MofParseNode::name)
                        .forEach { special ->
                            val specialName = special.name.asJavaClassName()
                            java(
                                """

|               @MetaValue(value = "$specialName", targetEntity = ${specialName}Impl.class), 
"""
                            )
                        }
                    java(
                        """

|       }),
"""
                    )
                }
                java(
                    """

})
@GenericGenerators(value = {
        @GenericGenerator(name = "UseExistingOrGenerateUUIDGenerator", strategy = "jpa.UseExistingOrGenerateUUIDGenerator")
})
package ${const.javaImplPackage};

import org.hibernate.annotations.AnyMetaDef;
import org.hibernate.annotations.AnyMetaDefs;
import org.hibernate.annotations.GenericGenerators;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.MetaValue;
"""
                )
            }
        }
    }
}

class ImplGeneratorCli : CliktCommand() {
    private val input: Path by option()
        .path(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)
        .required()
    private val output: Path by option()
        .path(mustExist = false, canBeFile = false, canBeDir = true)
        .required()

    override fun run() {
        Files.createDirectories(output)
        generateImpl(input, output)
    }
}

fun main(args: Array<String>) = ImplGeneratorCli().main(args)