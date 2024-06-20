package org.example.util.compiler

import java.io.IOException
import java.net.URI
import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject

/**
 * Inspired from https://github.com/trung/InMemoryJavaCompiler/blob/master/src/main/java/org/mdkt/compiler/SourceCode.java
 */
class SourceCode(className: String, private val contents: String?) :
    SimpleJavaFileObject(
        URI.create(
            "string:///" + className.replace('.', '/')
                    + JavaFileObject.Kind.SOURCE.extension
        ), JavaFileObject.Kind.SOURCE
    ) {

    @Throws(IOException::class)
    override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence {
        return (contents)!!
    }
}