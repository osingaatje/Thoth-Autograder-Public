package org.example.util.compiler

import org.example.util.inmemorycompiler.CompiledCode


class DynamicClassLoader(parent: ClassLoader?) : ClassLoader(parent) {
    private val customCompiledCode: MutableMap<String, CompiledCode> = HashMap()

    fun addCode(cc: CompiledCode) {
        customCompiledCode[cc.name] = cc
    }

    @Throws(ClassNotFoundException::class)
    override fun findClass(name: String): Class<*> {
        val cc = customCompiledCode[name] ?: return super.findClass(name)
        val byteCode = cc.byteCode
        return defineClass(name, byteCode, 0, byteCode.size)
    }
}