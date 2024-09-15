package com.morizero.h2cs.api

import com.morizero.h2cs.model.APIInfo
import com.morizero.h2cs.model.Context
import com.morizero.h2cs.model.Parameter

fun Parameter.toCS(ctx: Context): String {
    val modifier: String
    val refTypeAttribute = attributes.firstOrNull { it.namespace == "milize" && it.name == "RefType" }
    if (refTypeAttribute != null) {
        when (refTypeAttribute.args[0].lowercase()) {
            "\"out\"" -> modifier = "out"
            "\"ref\"" -> modifier = "ref"
            else -> throw Exception("Invalid RefType value: ${refTypeAttribute.args[0]}")
        }
    } else if (isReference) {
        modifier = "out"
    } else {
        modifier = ""
    }

    val printedType = attributes.firstOrNull { it.namespace == "milize" && it.name == "CSharpType" }.let {
        if (it == null) {
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

    return "${modifier} ${printedType} ${name}"
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
            [DllImport(dllName, EntryPoint = EntryPointPrefix + "${cSymbolName}")]
            internal static extern unsafe ${ctx.resolveCPPTypeToCSType(returnType)} ${methodName}(${parameterList});
            ${macro.second}
""".trimIndent()
}
