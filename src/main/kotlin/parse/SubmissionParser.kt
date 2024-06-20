package org.example.parse

import com.github.javaparser.ParseProblemException
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.MethodDeclaration


data class ParsedSubmission(val compilationUnit: CompilationUnit?, val methodDeclaration: MethodDeclaration?) {
    init {
        if (compilationUnit == null && methodDeclaration == null)
            error("Cannot instantiate a parsed submission without a CU and a MethodDeclaration!")

        if (compilationUnit != null && methodDeclaration != null)
            error("Cannot instantiate a parsed submission with BOTH a CU and a MethodDeclaration")
    }

    override fun toString(): String {
        return if (this.compilationUnit != null)
            "ParsedSubmission (cu): <<<\n${this.compilationUnit}>>>"
        else if (this.methodDeclaration != null)
            "ParsedSubmission (methodDeclaration): <<<${this.methodDeclaration}>>>"
        else
            error("Invalid ParsedSubmission (both cu and methodDeclaration were null???)")
    }
}

interface SubmissionParser {
    fun parseSubmission(submission: String): ParsedSubmission?
}

object SubmissionParserJava : SubmissionParser {
    /**
     * Tries to parse submission. Tries to parse a normal Java class.
     * When that fails (due to absence of a wrapper Class for example), tries to parse only a function.
     */
    override fun parseSubmission(submission: String): ParsedSubmission? {
        try {
            return ParsedSubmission(StaticJavaParser.parse(submission), null)
        } catch (_: ParseProblemException) {
        }
        // apparently the student submission is not wrapped inside a class. Let's try parsing only a function
        try {
            return ParsedSubmission(null, StaticJavaParser.parseMethodDeclaration(submission))
        } catch (_: ParseProblemException) {
        }
        return null
    }
}

