package org.example.parse.repair.util

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import org.example.parse.repair.Constants

object ParseUtil {
    fun makeNewCUWithClass(ast : ClassOrInterfaceDeclaration) : CompilationUnit {
        val newCU = CompilationUnit()
        Constants.ADD_IMPORTS_IF_NOT_EXIST.forEach { newCU.addImport(it) }

        // copy existing class to compilation unit
        val newClass = newCU.addClass(ast.name.asString(), *ast.modifiers.map { it.keyword }.toTypedArray())
        newClass.setMembers(ast.members)
        newClass.setAbstract(ast.isAbstract)
        newClass.setInterface(ast.isInterface)
        newClass.setFinal(ast.isFinal)

        return newCU
    }

    fun tryFindExistingCU(classOrInterface: ClassOrInterfaceDeclaration) : CompilationUnit? {
        var existingCU: CompilationUnit? = null
        classOrInterface.findCompilationUnit().ifPresent {
            existingCU = it
        }
        return existingCU
    }
}