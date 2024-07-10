package party.para.tasks

import com.morizero.h2cs.H2CSVisitor
import com.morizero.h2cs.H2CSVisitorExtension.writeCSharpBinding
import com.morizero.h2cs.H2CSVisitorExtension.writeFrameworkBinding
import com.morizero.h2cs.generated.parser.CPP14Lexer
import com.morizero.h2cs.generated.parser.CPP14Parser
import com.morizero.h2cs.model.SimpleContext
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.gradle.api.DefaultTask
import org.gradle.api.NonNullApi
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import party.para.util.takeIfNotNull
import java.io.File

@NonNullApi
@CacheableTask
abstract class H2CSTask : DefaultTask() {
    @Input
    var projectName: String = ""

    @Input
    var sourceFilePath: String = ""

    @Input
    @Optional
    var csharpBindingOutputPath: String? = null

    @Input
    @Optional
    var cppFrameworkBindingOutputPath: String? = null

    @Input
    var typeMapping: MutableMap<List<String>, String> = SimpleContext.DEFAULT_TYPE_MAPPING.toMutableMap()

    private fun <K, V> MutableMap<K, V>.putFailIfExists(k: K, v: V) {
        if (containsKey(k)) {
            throw Exception("$k already exists!")
        }
        set(k, v)
    }

    @JvmName("addTypeMapping_StringList_String")
    fun addTypeMapping(k: List<String>, v: String) {
        typeMapping.putFailIfExists(k, v)
    }

    @JvmName("addTypeMapping_String_String")
    fun addTypeMapping(k: String, v: String) {
        addTypeMapping(listOf(k), v)
    }

    @JvmName("addTypeMapping_StringListStringPairVararg")
    fun addTypeMapping(vararg l: Pair<List<String>, String>) {
        l.forEach { addTypeMapping(it.first, it.second) }
    }

    @JvmName("addTypeMapping_StringStringPairVararg")
    fun addTypeMapping(vararg l: Pair<String, String>) {
        l.forEach { addTypeMapping(it.first, it.second) }
    }

    @TaskAction
    fun execute() {
        if (projectName.isEmpty()) {
            throw RuntimeException("project name is null or empty")
        }

        if (!File(sourceFilePath).exists()) {
            throw RuntimeException("Source file does not exist")
        }

        val file = File(sourceFilePath)
        val input = CharStreams.fromReader(file.reader())
        val lexer = CPP14Lexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = CPP14Parser(tokens)
        val tree = parser.translationUnit()

        val visitor = H2CSVisitor(input)
        visitor.visit(tree)

        val ctx = SimpleContext(projectName)
        ctx.typeMapping = typeMapping
        csharpBindingOutputPath.takeIfNotNull {
            visitor.writeCSharpBinding(ctx, File(it))
        }
        cppFrameworkBindingOutputPath.takeIfNotNull {
            visitor.writeFrameworkBinding(ctx, File(it))
        }
    }
}

