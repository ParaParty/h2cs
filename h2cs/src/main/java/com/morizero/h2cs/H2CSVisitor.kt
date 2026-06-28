package com.morizero.h2cs


import com.morizero.h2cs.generated.parser.CPP14Parser
import com.morizero.h2cs.generated.parser.CPP14ParserBaseVisitor
import com.morizero.h2cs.model.APIInfo
import com.morizero.h2cs.model.Attribute
import com.morizero.h2cs.model.Context
import com.morizero.h2cs.model.Parameter
import com.morizero.h2cs.model.ReturnType
import com.morizero.h2cs.model.TypeInfo
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.misc.Interval

class H2CSVisitor(val ctx: Context) : CPP14ParserBaseVisitor<Unit>() {
    val input: CodePointCharStream = ctx.inputCodePointCharStream!!
    val apiList = mutableListOf<APIInfo>()
    val publicApiAnnotation: String = ctx.projectName.uppercase() + "_API"

    private fun parseAttribute(attribute: List<CPP14Parser.AttributeContext>): List<Attribute> {
        val attributes = mutableListOf<Attribute>()
        for (attr in attribute) {
            val item = Attribute()
            item.namespace = attr.attributeNamespace()?.text ?: ""
            item.name = attr.Identifier().text
            item.args = attr.attributeArgumentClause()?.balancedTokenSeq()?.balancedToken()?.map { it.text } ?: listOf()
            attributes.add(item)
        }
        return attributes
    }

    private fun parseAttributeSpecifierSeq(attributeSpecifierSeq: CPP14Parser.AttributeSpecifierSeqContext?): List<Attribute> {
        return attributeSpecifierSeq?.attributeSpecifier()?.flatMap {
            parseAttribute(it.attributeList().attribute())
        } ?: listOf()
    }

    private fun <T : TypeInfo> parseTypeInfo(
        typeInfo: T,
        declSpecifiers: List<CPP14Parser.DeclSpecifierContext>,
        pointerDeclarator: CPP14Parser.PointerDeclaratorContext,
        attributes: List<Attribute> = listOf(),
    ): T {
        typeInfo.type = declSpecifiers.map { it.text }
        typeInfo.attributes = attributes
        typeInfo.pointerOperators = pointerDeclarator.pointerOperator().map { it.text }
        typeInfo.isReference = pointerDeclarator.pointerOperator().any { it.text.contains("&") }
        typeInfo.isPointer = pointerDeclarator.pointerOperator().any { it.text.contains("*") }
        return typeInfo
    }

    private fun parseParameter(
        declSpecifiers: List<CPP14Parser.DeclSpecifierContext>,
        pointerDeclarator: CPP14Parser.PointerDeclaratorContext,
        attributes: List<Attribute> = listOf(),
    ): Parameter {
        val parameter = parseTypeInfo(Parameter(), declSpecifiers, pointerDeclarator, attributes)
        parameter.name = pointerDeclarator.noPointerDeclarator().text
        return parameter
    }

    private fun parseReturnType(
        declSpecifiers: List<CPP14Parser.DeclSpecifierContext>,
        pointerDeclarator: CPP14Parser.PointerDeclaratorContext,
        attributes: List<Attribute> = listOf(),
    ): ReturnType {
        return parseTypeInfo(ReturnType(), declSpecifiers, pointerDeclarator, attributes)
    }

    override fun visitSimpleDeclaration(ctx: CPP14Parser.SimpleDeclarationContext) {
        val ret = APIInfo()

        val declarationAttributes = parseAttributeSpecifierSeq(ctx.attributeSpecifierSeq())
        if (declarationAttributes.any { it.namespace == "milize" && it.name == "CSharpIgnore" }) {
            return
        }

        val declSpecifierSeq = ctx.declSpecifierSeq()
        val declSpecifiers = declSpecifierSeq.declSpecifier()

        ret.modifier = declSpecifiers.dropLast(1).map { it.text }

        val pointerDeclarator = ctx.initDeclaratorList().initDeclarator(0).declarator().pointerDeclarator()
        val noPointerDeclarator = pointerDeclarator.noPointerDeclarator()
        val returnTypeDeclSpecifiers = declSpecifiers.filter { it.text != publicApiAnnotation }
        ret.returnType = parseReturnType(
            declSpecifiers = returnTypeDeclSpecifiers,
            pointerDeclarator = pointerDeclarator,
            attributes = declarationAttributes,
        )
        ret.functionName = noPointerDeclarator.noPointerDeclarator().text
        val parametersAndQualifiers = noPointerDeclarator.parametersAndQualifiers()
        val parameterDeclarationClause = parametersAndQualifiers.parameterDeclarationClause()
        ret.parameters = parameterDeclarationClause?.parameterDeclarationList()?.parameterDeclaration()?.map {
            val pointerDeclarator = it.declarator().pointerDeclarator()
            parseParameter(
                declSpecifiers = it.declSpecifierSeq().declSpecifier(),
                pointerDeclarator = pointerDeclarator,
                attributes = parseAttributeSpecifierSeq(it.attributeSpecifierSeq()),
            )
        } ?: listOf()

        apiList += ret


        val funcDeclStart = ctx.start.startIndex
        val funcDeclStop = ctx.stop.stopIndex

        val declSpecifierSeqStop = ctx.stop.stopIndex

        val funcNameStart = noPointerDeclarator.noPointerDeclarator().start.startIndex
        val funcNameStop = noPointerDeclarator.noPointerDeclarator().stop.stopIndex

        val functionName = input.getText(Interval(funcNameStart, funcNameStop));
        ret.frameworkDeclarationRest = input.getText(Interval(funcNameStop + 1, funcDeclStop - 1))
    }
}
