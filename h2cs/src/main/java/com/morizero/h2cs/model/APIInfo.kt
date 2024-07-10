package com.morizero.h2cs.model


class APIInfo {
    var modifier: List<String> = listOf()
    var returnType: String = ""
    var functionName: String = ""
    var parameters: List<Parameter> = listOf()
    var attributes: List<Attribute> = listOf()
}
