package org.example.parse.repair.fixers

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.NodeList
import org.example.parse.ParsedSubmission
import org.example.parse.repair.Constants

object ImportFixer : Formatter {
    /**
     * Adds the default Java imports for HashMaps, Lists, Sets etc. to the class definition if not already there.
     * Throws all kinds of funky errors if you're not passing a Class into here, so I suggest using the `ClassWrapper` before this one :)
     */
    override fun formatSubmission(submission: ParsedSubmission): ParsedSubmission {
        if (submission.compilationUnit != null) {
            return ParsedSubmission(fixCU(submission.compilationUnit), null)
        }

        error("Cannot fix imports of something that is not a Compilation Unit!")
    }

    private fun fixCU(submissionAST: CompilationUnit): CompilationUnit {
        val ignoredImports = submissionAST.imports.filter { Constants.IGNORE_IMPORTS.contains(it.name.asString()) }
        submissionAST.imports = NodeList() // RESET ALL IMPORTS

        // add the ignored imports back:
        ignoredImports.forEach { submissionAST.addImport(it.nameAsString) }
        // add the "always add this import" imports:
        Constants.ADD_IMPORTS_IF_NOT_EXIST.forEach { submissionAST.addImport(it) }
        return submissionAST
    }
}