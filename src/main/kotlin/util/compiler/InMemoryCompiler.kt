package org.example.util.compiler

import org.example.util.inmemorycompiler.*
import java.util.*
import javax.tools.*

/**
 * Compile Java sources in-memory
 */
class InMemoryJavaCompiler {
    private val javac: JavaCompiler = ToolProvider.getSystemJavaCompiler()
    private var classLoader: DynamicClassLoader
    private var defaultOptions : List<String> = listOf("-Xlint:-options", "-Xlint:unchecked") // disable warning about annotation compiling
    private var options: List<String>? = null
    private var ignoreWarnings: Boolean = false

    private val sourceCodes: MutableMap<String, SourceCode> = HashMap<String, SourceCode>()

    init {
        this.classLoader = DynamicClassLoader(ClassLoader.getSystemClassLoader())
    }

    fun useParentClassLoader(parent: ClassLoader?): InMemoryJavaCompiler {
        this.classLoader = DynamicClassLoader(parent)
        return this
    }

    /**
     * Options used by the compiler, e.g. '-Xlint:unchecked'.
     */
    fun useOptions(vararg options: String): InMemoryJavaCompiler {
        this.options = listOf(*options)
        return this
    }

    /**
     * Ignore non-critical compiler output, like unchecked/unsafe operation warnings.
     */
    fun ignoreWarnings(): InMemoryJavaCompiler {
        ignoreWarnings = true
        return this
    }

    /**
     * Compiles all sources (put them into the compiler with addSource())
     *
     * @return Map containing instances of all compiled classes
     * @throws CompilationException
     */
    @Throws(CompilationException::class, ClassNotFoundException::class)
    fun compileAll(): Map<String, Class<*>> {
        if (sourceCodes.isEmpty()) {
            throw CompilationException("No source code to compile")
        }
        val compilationUnits: Collection<SourceCode> = sourceCodes.values

        val code: Array<CompiledCode?> = arrayOfNulls(compilationUnits.size)
        val iter: Iterator<SourceCode> = compilationUnits.iterator()
        for (i in code.indices) {
            code[i] = CompiledCode(iter.next().javaClass.simpleName) // TODO: was "name": Check if correct name
        }
        val diagnosticsCollector = DiagnosticCollector<JavaFileObject>()
        val fileManager = ExtendedStandardJavaFileManager(javac.getStandardFileManager(null, null, null), classLoader)

        val allOptions : List<String> = if (options != null) options!! + defaultOptions else defaultOptions
        val compilationTask = javac.getTask(null, fileManager, diagnosticsCollector, allOptions, null, compilationUnits)
        val result = compilationTask.call()

        if (!result || diagnosticsCollector.diagnostics.size > 0) {
            val exceptionMsg = StringBuffer()
            exceptionMsg.append("Unable to compile the source")
            var hasWarnings = false
            var hasErrors = false
            for (d in diagnosticsCollector.diagnostics) {
                when (d.kind) {
                    Diagnostic.Kind.NOTE, Diagnostic.Kind.MANDATORY_WARNING, Diagnostic.Kind.WARNING -> hasWarnings =
                        true

                    Diagnostic.Kind.OTHER, Diagnostic.Kind.ERROR -> hasErrors = true
                    else -> hasErrors = true
                }
                exceptionMsg.append("\n").append("[kind=").append(d.kind)
                exceptionMsg.append(", ").append("line=").append(d.lineNumber)
                exceptionMsg.append(", ").append("message=").append(d.getMessage(Locale.US)).append("]")
            }
            if (hasWarnings && !ignoreWarnings || hasErrors) {
                throw CompilationException(exceptionMsg.toString())
            }
        }

        val classes: MutableMap<String, Class<*>> = HashMap()
        for (className in sourceCodes.keys) {
            classes[className] = classLoader.loadClass(className)
        }
        return classes
    }

    /**
     * Compile single source
     *
     * @param className
     * @param sourceCode
     * @return
     * @throws Exception
     */
    @Throws(CompilationException::class, ClassNotFoundException::class)
    fun compile(className: String, sourceCode: String?): Class<*> {
        return addSource(className, sourceCode).compileAll()[className]!!
    }

    /**
     * Add source code to the compiler
     *
     * @param className
     * @param sourceCode
     * @see compileAll
     */
    fun addSource(className: String, sourceCode: String?): InMemoryJavaCompiler {
        sourceCodes[className] = SourceCode(className, sourceCode)
        return this
    }
}