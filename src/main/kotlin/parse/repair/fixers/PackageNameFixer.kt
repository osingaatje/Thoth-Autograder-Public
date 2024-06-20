package org.example.parse.repair.fixers

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import org.example.parse.ParsedSubmission
import org.example.parse.repair.Constants
import org.example.parse.repair.util.ParseUtil.makeNewCUWithClass
import org.example.parse.repair.util.ParseUtil.tryFindExistingCU

object PackageNameFixer : Formatter {
    private fun fixCU(submissionAST: CompilationUnit): CompilationUnit {
        return submissionAST.setPackageDeclaration(Constants.SUBMISSION_PACKAGE_NAME)
    }

    private fun fixClassOrInterfaceDecl(submissionAST: ClassOrInterfaceDeclaration) : CompilationUnit {
        val existingCU : CompilationUnit? = tryFindExistingCU(submissionAST)
        if (existingCU != null)
            return existingCU

        val newCU = makeNewCUWithClass(submissionAST)
        return fixCU(newCU)
    }

    override fun formatSubmission(submission: ParsedSubmission): ParsedSubmission {
        if (submission.compilationUnit != null) {
            return ParsedSubmission((fixCU(submission.compilationUnit)), null)
        }

        error("Cannot fix package name of something that is not a compilation unit!")
    }

}