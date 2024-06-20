package org.example.parse.repair.fixers

import org.example.parse.ParsedSubmission
import kotlin.jvm.Throws

interface Formatter {
    @Throws(IllegalStateException::class)
    fun formatSubmission(submission : ParsedSubmission) : ParsedSubmission
}