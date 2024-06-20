package org.example.parse.repair

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

object Constants {
    const val SUBMISSION_PACKAGE_NAME : String =  "analysis.dynamic_analysis" //"student.submission"
    const val SUBMISSION_CLASS_NAME : String = "ExerciseClass"


    // Section IMPORTS: Add imports if they do not exist yet, remove every other import except the IGNORE_IMPORTS list.
    val ADD_IMPORTS_IF_NOT_EXIST : ImmutableList<String> = persistentListOf(
        "java.util.HashMap",
        "java.util.Map",
        "java.util.ArrayList",
        "java.util.Locale" // for toLowerCase(Locale.ROOT) etc.
    )
    // Imports to not remove. Be wary that strings without a '.' ("testImport" for example) will not be re-added to the imports, and for some reason JavaParser does not throw errors when failing to add imports :(
    val IGNORE_IMPORTS : ImmutableList<String> = persistentListOf(
        // TODO: Add ignore imports in the future?
    )
}