package com.morizero.h2cs.model

interface Context {
    val projectName: String

    fun resolveCPPTypeToCSType(cppType: List<String>?): String

    fun resolveCPPTypeToCSType(cppType: String): String {
        return resolveCPPTypeToCSType(listOf(cppType))
    }
}

class SimpleContext(projectName: String) : Context {
    companion object {
        val DEFAULT_TYPE_MAPPING: Map<List<String>, String> = mapOf<List<String>, String>(
            listOf("uint8_t") to "byte",

            listOf("uint32_t") to "uint",
            listOf("int32_t") to "int",

            listOf("uint16_t") to "ushort",
            listOf("int16_t") to "short",

            listOf("uint64_t") to "ulong",
            listOf("int64_t") to "long",

            listOf("size_t") to "ulong",

            listOf("bool") to "bool",
            listOf("float") to "float",
            listOf("double") to "double",
        )
    }

    override var projectName: String = ""

    init {
        if (projectName.isEmpty()) {
            throw IllegalArgumentException("Project name cannot be empty")
        }
        this.projectName = projectName
    }

    var typeMapping: Map<List<String>, String> = DEFAULT_TYPE_MAPPING

    override fun resolveCPPTypeToCSType(cppType: List<String>?): String {
        return typeMapping[cppType] ?: throw IllegalStateException("unknown type: ${cppType}")
    }
}
