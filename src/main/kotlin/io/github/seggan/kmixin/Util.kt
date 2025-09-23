package io.github.seggan.kmixin

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

val KSType.javaType: String
    get() = when (val asString = declaration.qualifiedName!!.asString()) {
        "kotlin.Int" -> "int"
        "kotlin.Float" -> "float"
        "kotlin.Double" -> "double"
        "kotlin.Long" -> "long"
        "kotlin.Short" -> "short"
        "kotlin.Byte" -> "byte"
        "kotlin.Boolean" -> "boolean"
        "kotlin.Char" -> "char"
        "kotlin.Unit" -> "void"
        "kotlin.String" -> "java.lang.String"
        "kotlin.Any" -> "java.lang.Object"
        "kotlin.Nothing" -> "void"
        "kotlin.IntArray" -> "int[]"
        "kotlin.FloatArray" -> "float[]"
        "kotlin.DoubleArray" -> "double[]"
        "kotlin.LongArray" -> "long[]"
        "kotlin.ShortArray" -> "short[]"
        "kotlin.ByteArray" -> "byte[]"
        "kotlin.BooleanArray" -> "boolean[]"
        "kotlin.CharArray" -> "char[]"
        else -> asString
    }

val KSType.javaBoxedType: String
    get() = when (val asString = declaration.qualifiedName!!.asString()) {
        "kotlin.Int" -> "java.lang.Integer"
        "kotlin.Float" -> "java.lang.Float"
        "kotlin.Double" -> "java.lang.Double"
        "kotlin.Long" -> "java.lang.Long"
        "kotlin.Short" -> "java.lang.Short"
        "kotlin.Byte" -> "java.lang.Byte"
        "kotlin.Boolean" -> "java.lang.Boolean"
        "kotlin.Char" -> "java.lang.Character"
        "kotlin.Unit" -> "void"
        "kotlin.String" -> "java.lang.String"
        "kotlin.Any" -> "java.lang.Object"
        "kotlin.Nothing" -> "void"
        "kotlin.IntArray" -> "int[]"
        "kotlin.FloatArray" -> "float[]"
        "kotlin.DoubleArray" -> "double[]"
        "kotlin.LongArray" -> "long[]"
        "kotlin.ShortArray" -> "short[]"
        "kotlin.ByteArray" -> "byte[]"
        "kotlin.BooleanArray" -> "boolean[]"
        "kotlin.CharArray" -> "char[]"
        else -> asString
    }

fun KSAnnotated.hasAnnotation(name: String) =
    annotations.any { it.annotationType.resolve().declaration.qualifiedName?.asString() == name }

fun KSAnnotation.toJava(): String {
    fun valueAsJava(value: Any?): String {
        return when (value) {
            is String -> "\"${value.replace("\"", "\\\"")}\""
            is Char -> "'${if (value == '\'') "\\'" else value}'"
            is Byte, is Short, is Int, is Long, is Float, is Double, is Boolean -> value.toString()
            is KSAnnotation -> value.toJava()
            is KSType -> value.javaType + ".class"
            is Array<*> -> value.joinToString(prefix = "{ ", postfix = " }", separator = ", ", transform = ::valueAsJava)
            is List<*> -> value.joinToString(prefix = "{ ", postfix = " }", separator = ", ", transform = ::valueAsJava)
            is KSClassDeclaration -> value.qualifiedName!!.asString()
            null -> "null"
            else -> throw IllegalArgumentException("Unsupported annotation value type: ${value::class.qualifiedName}")
        }
    }

    val sb = StringBuilder("@")
    sb.append(annotationType.resolve().javaType).append('(')
    for ((i, arg) in arguments.withIndex()) {
        if (arg in defaultArguments) continue
        if (arg.name != null) {
            sb.append(arg.name!!.asString()).append(" = ")
        }
        sb.append(valueAsJava(arg.value))
        if (i < arguments.size - 1) sb.append(", ")
    }
    if (sb.last() != '(') sb.setLength(sb.length - 2) // Remove last ", "
    sb.append(')')
    return sb.toString()
}