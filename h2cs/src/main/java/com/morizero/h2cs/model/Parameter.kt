package com.morizero.h2cs.model


class Parameter {
    var isPointer: Boolean = false
    var isReference: Boolean = false
    var type: List<String> = listOf()
    var name: String = ""
    var attributes: List<Attribute> = listOf()
}