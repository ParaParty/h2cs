package com.morizero.h2cs.api

import com.morizero.h2cs.model.APIInfo
import com.morizero.h2cs.model.Context
import com.morizero.h2cs.model.Parameter
import com.morizero.h2cs.model.TypeInfo

fun Parameter.toCS(ctx: Context): String {
    val modifier: String
    val refTypeAttribute = attributes.firstOrNull { it.namespace == "milize" && it.name == "RefType" }
    modifier = if (refTypeAttribute != null) {
        when (refTypeAttribute.args[0].lowercase()) {
            "\"out\"" -> "out"
            "\"ref\"" -> "ref"
            else -> throw Exception("Invalid RefType value: ${refTypeAttribute.args[0]}")
        }
    } else if (isReference) {
        "out"
    } else {
        ""
    }

    return listOf(modifier, toCSType(ctx), name).filter { it.isNotEmpty() }.joinToString(" ")
}

fun TypeInfo.toCSType(ctx: Context): String {
    val printedType = attributes.firstOrNull { it.namespace == "milize" && it.name == "CSharpType" }.let {
        if (it == null) {
            if (type == listOf("void") && !isPointer) {
                return "void"
            }
            var t = ctx.resolveCPPTypeToCSType(type)
            if (t == "IntPtr") {
                if (!isPointer) {
                    throw Exception("try to mapping a non pointer declaration to IntPtr")
                }
            } else {
                if (isPointer) {
                    t = "$t[]"
                }
            }
            t
        } else {
            it.args[0].let { it.substring(1, it.length - 1) }
        }
    }

    return printedType
}

fun TypeInfo.toCPPType(): String {
    return (type + pointerOperators).joinToString(" ")
}


fun APIInfo.toCS(ctx: Context): String {
    val cSymbolName = functionName
    val methodName = if (cSymbolName.startsWith(ctx.projectName)) {
        cSymbolName.substring(ctx.projectName.length)
    } else {
        cSymbolName
    }

    val parameterList = parameters.map { it.toCS(ctx) }.joinToString(",\n")

    val macro = run {
        if (attributes.any { it.namespace == "milize" && it.name == "EditorOnly" }) {
            "#if UNITY_EDITOR || MILTHM_EDITOR" to "#endif"
        } else if (attributes.any { it.namespace == "milize" && it.name == "GameOnly" }) {
            "#if !(UNITY_EDITOR || MILTHM_EDITOR)" to "#endif"
        } else {
            "" to ""
        }
    }

    return """
            ${macro.first}
            [DllImport(dllName, EntryPoint = EntryPointPrefix + "$cSymbolName")]
            internal static extern unsafe ${returnType.toCSType(ctx)} ${methodName}(${parameterList});
            ${macro.second}
""".trimIndent()
}

fun APIInfo.toFrameworkBinding(): String {
    if (attributes.any { it.namespace == "milize" && it.name == "EditorOnly" }) {
        return ""
    }

    val bindingFunctionName = "FrameworkBinding$functionName"
    val frameworkCall = parameters.map { it.name }.joinToString(separator = ", ", prefix = "$functionName(", postfix = ")")
    val body = if (returnType.type == listOf("void") && !returnType.isPointer) {
        "${frameworkCall};"
    } else {
        "return ${frameworkCall};"
    }

    return """
            ${returnType.toCPPType()} ${bindingFunctionName} ${frameworkDeclarationRest} {
                ${body}
            }
        """.trimIndent()
}
