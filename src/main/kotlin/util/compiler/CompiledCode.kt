package org.example.util.inmemorycompiler

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.URI
import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject


/**
 * Inspired from https://github.com/trung/InMemoryJavaCompiler/blob/master/src/main/java/org/mdkt/compiler/CompiledCode.java
 */
class CompiledCode(className: String) : SimpleJavaFileObject(URI(className), JavaFileObject.Kind.CLASS) {
    private val byteArrOutput = ByteArrayOutputStream()

    @Throws(IOException::class)
    override fun openOutputStream(): OutputStream {
        return byteArrOutput
    }

    val byteCode: ByteArray
        get() = byteArrOutput.toByteArray()
}