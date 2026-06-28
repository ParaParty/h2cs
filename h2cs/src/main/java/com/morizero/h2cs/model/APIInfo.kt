package com.morizero.h2cs.model


class APIInfo {
    var modifier: List<String> = listOf()
    var returnType: ReturnType = ReturnType()
    var functionName: String = ""
    var frameworkDeclarationRest: String = ""
    var parameters: List<Parameter> = listOf()
    val attributes: List<Attribute>
        get() = returnType.attributes
}
