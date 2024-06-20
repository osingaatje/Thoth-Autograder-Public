package org.example.parse.repair

import org.example.parse.ParsedSubmission
import org.example.parse.repair.fixers.*
import kotlin.reflect.KClass

object SubmissionFormatter : Formatter {
    private val formatters : List<KClass<out Formatter>> = listOf(ClassWrapperFixer::class, ImportFixer::class, PackageNameFixer::class)

    @Throws(IllegalStateException::class)
    /**
    * Formats a student submission for dynamic grading by using the fixers in `parse.repair.fixers` to change class names, package declarations etc.
    */
    override fun formatSubmission(submission: ParsedSubmission): ParsedSubmission {
        var tree = submission // convert submissionAST to a variable (is val by default)
        for (formatter in formatters) { // reflectively create instances of formatters and format the submissions
            tree = formatter.objectInstance?.formatSubmission(tree)
                ?: error("Could not get object instance of object \"$formatter\".")
        }

        return tree
    }
}