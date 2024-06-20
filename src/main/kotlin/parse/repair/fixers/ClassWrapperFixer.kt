package org.example.parse.repair.fixers

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.SimpleName
import org.example.parse.ParsedSubmission
import org.example.parse.repair.Constants

object ClassWrapperFixer : Formatter {
    private fun formatNode(ast : Node) : CompilationUnit {
        when(ast) {
            is CompilationUnit -> {
                // TODO: copy the compilation unit and recursively format all the 'type's?
                // for now, just assume it's only one function or class and just do that:
                if (ast.types.size != 1) {
                    error("The ClassWrapper does not support multiple classes or functions yet!")
                }

                ast.types[0].setName(Constants.SUBMISSION_CLASS_NAME) // set name of class
                return ast
            }

            is MethodDeclaration -> {
                val wrapperCompilationUnit = CompilationUnit()
                val wrapperClass = ClassOrInterfaceDeclaration()
                wrapperClass.name = SimpleName(Constants.SUBMISSION_CLASS_NAME)

                // add method (name + modifiers)
                val newMethod = wrapperClass.addMethod(ast.nameAsString, *ast.modifiers.map { it.keyword }.toTypedArray())

                // set return type
                newMethod.setType(ast.type)
                // set arguments (parameters)
                newMethod.setParameters(ast.parameters)

                // set body
                ast.body.ifPresent {
                    newMethod.setBody(it)
                }

                // set class as type of wrapper compilation unit
                wrapperCompilationUnit.types = NodeList(wrapperClass)

                return wrapperCompilationUnit
            }

            else -> error("Submission was neither a function nor a Class or Interface!")
        }
    }

    /**
     * Replaces all class declarations with the `Constants.SUBMISSION_CLASS_NAME`
     * If the submission is a function, wraps the function in a class with `Constants.SUBMISSION_CLASS_NAME`
     */
    override fun formatSubmission(submission: ParsedSubmission): ParsedSubmission {
        val formattedRes : CompilationUnit = formatNode(submission.compilationUnit ?: (submission.methodDeclaration ?: error("Invalid submission AST!")))
        return ParsedSubmission(formattedRes, null)
    }
}