package com.morizero.h2cs.cmd

import com.morizero.h2cs.H2CSVisitor
import com.morizero.h2cs.H2CSVisitorExtension.writeCSharpBinding
import com.morizero.h2cs.H2CSVisitorExtension.writeFrameworkBinding
import com.morizero.h2cs.generated.parser.CPP14Lexer
import com.morizero.h2cs.generated.parser.CPP14Parser
import com.morizero.h2cs.model.SimpleContext
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val projectName = args[0]

        val file = File(args[1])
        val input = CharStreams.fromReader(file.reader())
        val lexer = CPP14Lexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = CPP14Parser(tokens)

        val tree = parser.translationUnit()
        val ctx = SimpleContext(projectName)
        ctx.inputCodePointCharStream = input

        val visitor = H2CSVisitor(ctx)
        visitor.visit(tree)


        visitor.writeCSharpBinding(ctx, File(args[2]))
        visitor.writeFrameworkBinding(ctx, File(args[3]))
    }
}
