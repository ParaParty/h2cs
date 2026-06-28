package com.morizero.h2cs.model


open class TypeInfo {
    var isPointer: Boolean = false
    var isReference: Boolean = false
    var type: List<String> = listOf()
    var pointerOperators: List<String> = listOf()
    var attributes: List<Attribute> = listOf()
}

class Parameter : TypeInfo() {
    var name: String = ""
}

class ReturnType : TypeInfo()
