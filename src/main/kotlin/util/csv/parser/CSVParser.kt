package org.example.util.csv.parser

import java.io.InputStream

interface CSVParser {
    fun parseSubmissions(inputStream: InputStream): List<Submission>
}