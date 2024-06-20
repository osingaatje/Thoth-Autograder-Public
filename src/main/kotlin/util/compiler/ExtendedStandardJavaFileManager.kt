package org.example.util.compiler

import org.example.util.inmemorycompiler.CompiledCode
import java.io.IOException
import javax.tools.FileObject
import javax.tools.ForwardingJavaFileManager
import javax.tools.JavaFileManager
import javax.tools.JavaFileObject


/**
 * Created by trung on 5/3/15. Edited by turpid-monkey on 9/25/15, completed
 * support for multiple compile units.
 */
open class ExtendedStandardJavaFileManager(
    fileManager: JavaFileManager?,
    private val cl: DynamicClassLoader
) : ForwardingJavaFileManager<JavaFileManager?>(fileManager) {
    private val compiledCode: MutableList<CompiledCode> = ArrayList()

    @Throws(IOException::class)
    override fun getJavaFileForOutput(
        location: JavaFileManager.Location, className: String,
        kind: JavaFileObject.Kind, sibling: FileObject
    ): JavaFileObject {
        try {
            val innerClass = CompiledCode(className)
            compiledCode.add(innerClass)
            cl.addCode(innerClass)
            return innerClass
        } catch (e: Exception) {
            throw RuntimeException(
                "Error while creating in-memory output file for "
                        + className, e
            )
        }
    }

    override fun getClassLoader(location: JavaFileManager.Location): ClassLoader {
        return cl
    }
}