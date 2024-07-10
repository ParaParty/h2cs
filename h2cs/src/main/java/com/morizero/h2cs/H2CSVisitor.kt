package com.morizero.h2cs


import com.morizero.h2cs.generated.parser.CPP14Parser
import com.morizero.h2cs.generated.parser.CPP14ParserBaseVisitor
import com.morizero.h2cs.model.APIInfo
import com.morizero.h2cs.model.Attribute
import com.morizero.h2cs.model.Parameter
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.misc.Interval

class H2CSVisitor(val input: CodePointCharStream) : CPP14ParserBaseVisitor<Unit>() {
    val apiList = mutableListOf<APIInfo>()
    val frameworkStaticBinding = mutableListOf<String>()

    private fun parseAttribute(attribute: List<CPP14Parser.AttributeContext>): List<Attribute> {
        val attributes = mutableListOf<Attribute>()
        for (attr in attribute) {
            val item = Attribute()
            item.namespace = attr.attributeNamespace()?.text ?: ""
            item.name = attr.Identifier().text
            item.args =
                attr.attributeArgumentClause()?.balancedTokenSeq()?.balancedtoken()?.map { it.text } ?: listOf()
            attributes.add(item)
        }
        return attributes
    }

    override fun visitSimpleDeclaration(ctx: CPP14Parser.SimpleDeclarationContext) {
        val ret = APIInfo()

        for (attrSpec in ctx.attributeSpecifierSeq()?.attributeSpecifier() ?: listOf()) {
            ret.attributes += parseAttribute(attrSpec.attributeList().attribute())
        }
        if (ret.attributes.any { it.namespace == "milize" && it.name == "CSharpIgnore" }) {
            return
        }

        val declSpecifierSeq = ctx.declSpecifierSeq()
        ret.modifier = declSpecifierSeq.declSpecifier().let { it.subList(0, it.size - 1).map { it.text }.toList() }
        ret.returnType = ctx.declSpecifierSeq().declSpecifier().last().text

        val noPointerDeclarator =
            ctx.initDeclaratorList().initDeclarator(0).declarator().pointerDeclarator().noPointerDeclarator()
        ret.functionName = noPointerDeclarator.noPointerDeclarator().text
        val parametersAndQualifiers = noPointerDeclarator.parametersAndQualifiers()
        val parameterDeclarationClause = parametersAndQualifiers.parameterDeclarationClause()
        ret.parameters = parameterDeclarationClause?.parameterDeclarationList()?.parameterDeclaration()?.map {
            val parameter = Parameter()

            val attributes = mutableListOf<Attribute>()
            for (attrSpec in it.attributeSpecifierSeq()?.attributeSpecifier() ?: listOf()) {
                attributes += parseAttribute(attrSpec.attributeList().attribute())
            }

            parameter.type = it.declSpecifierSeq().declSpecifier().map { it.text }
            parameter.attributes = attributes
            parameter.name = it.declarator().pointerDeclarator().noPointerDeclarator().text
            val pointerOperator = it.declarator().pointerDeclarator().text;
            parameter.isReference = pointerOperator.contains("&")
            parameter.isPointer = pointerOperator.contains("*")
            parameter
        } ?: listOf()

        apiList += ret


        val declSpecifier = declSpecifierSeq.declSpecifier()

        val funcDeclStart = ctx.start.startIndex
        val funcDeclStop = ctx.stop.stopIndex

        val declSpecifierSeqStop = ctx.stop.stopIndex

        val funcNameStart = noPointerDeclarator.noPointerDeclarator().start.startIndex
        val funcNameStop = noPointerDeclarator.noPointerDeclarator().stop.stopIndex

        val bindingDeclTypeInfo = declSpecifier.map {
            val start = it.start.startIndex
            val stop = it.stop.stopIndex
            input.getText(Interval(start, stop))
        }.filter { it != "MILIZE_API" }.joinToString(separator = " ", prefix = "", postfix = " ")

        val functionName = input.getText(Interval(funcNameStart, funcNameStop));
        val bindingDeclFunctionName = "FrameworkBinding$functionName";
        val declFunctionRestPart = input.getText(Interval(funcNameStop + 1, funcDeclStop - 1))
        val frameworkCall = "${functionName}${
            ret.parameters.map { it.name }.joinToString(separator = ", ", prefix = "(", postfix = ")")
        }"

        if (!ret.attributes.any { it.namespace == "milize" && it.name == "EditorOnly" }) {
            frameworkStaticBinding += """
            ${bindingDeclTypeInfo} ${bindingDeclFunctionName} ${declFunctionRestPart} {
                return ${frameworkCall};
            }
        """.trimIndent()
        }
    }
}
