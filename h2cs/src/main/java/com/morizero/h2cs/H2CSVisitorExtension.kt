package com.morizero.h2cs

import com.morizero.h2cs.api.toCS
import com.morizero.h2cs.model.Context
import java.io.File

object H2CSVisitorExtension {
    fun H2CSVisitor.writeCSharpBinding(ctx: Context, outFile: File) {
        val result = """using System;
using System.Runtime.InteropServices;

namespace ${ctx.projectName}.Binding
{
    public class BindingC
    {
#if UNITY_IOS && !UNITY_EDITOR
        private const string dllName = "__Internal";
        private const string EntryPointPrefix = "FrameworkBinding";
#else
        private const string dllName = "lib${ctx.projectName}";
        private const string EntryPointPrefix = "";
#endif

${apiList.joinToString("\n\n") { it.toCS(ctx) }}

    }
}
"""

        outFile.printWriter().let {
            it.print(result)
            it.close()
        }
    }

    fun H2CSVisitor.writeFrameworkBinding(ctx: Context, outFile: File) {
        val result = """#include "${ctx.projectName}/${ctx.projectName.lowercase()}_game_interface.h"

extern "C" {
${frameworkStaticBinding.joinToString("\n\n")}
}
"""

        outFile.printWriter().let {
            it.print(result)
            it.close()
        }
    }
}
