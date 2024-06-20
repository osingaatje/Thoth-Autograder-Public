package org.example.util.csv.parser

import java.io.FileInputStream
import java.io.InputStream
import kotlin.jvm.Throws

const val MAX_CHARACTERS_TO_READ: Int = 32_000_000 // actual number for the dataset input file was 861949
const val LINE_SEPARATOR: Char = '\n' // Assuming LF formatting (i.e. '\n' and not '\r\n')
const val STRING_BUFFER_CAPACITY: Int = DEFAULT_BUFFER_SIZE // max characters the parser can read
const val CSV_NUM_COLS: Int = 19

object CSVParserRemindo : CSVParser {

    /**
     * Reads the submission in from the Remindo-exported Dataset.
     * NOTE: The submission uses `""` as string escape character (instead of `\"`), and contains newlines for code submission (which is why we have a more complicated parser)
     */
    @Throws(IllegalStateException::class)
    override fun parseSubmissions(inputStream: InputStream): List<Submission> {
        // Idea: count commas, unless in Strings (denoted by "string content"). This will allow us to just read in newlines and be fine.

        val reader = inputStream.bufferedReader()
        val header = reader.readLine()

        // verify column count
        val colCount = header.split(",").size
        if (colCount != CSV_NUM_COLS)
            error("Unexpected number of columns ($colCount v.s. expected $CSV_NUM_COLS\n\tCols: $header")


        val result: MutableList<Submission> = mutableListOf()
        val strCollector = CharArray(STRING_BUFFER_CAPACITY)
        // Index of strCollector. Will get incremented in the while loop to '0' and further. Points to *current* last char in collector.
        var index: Int = -1

        // collect elements of a row until we reach the limit of commas
        val colCollector: MutableList<String> = mutableListOf()

        var inString = false // denotes whether we are inside a string ("this kinda string")
        var parsedQuote = false // keeps whether we've parsed a quote as previous character
        var maxCharacterCounter = 0
        while (true) {
            index++
            maxCharacterCounter++

            if (maxCharacterCounter >= MAX_CHARACTERS_TO_READ)
                error("File apparently has more characters than allowed ($MAX_CHARACTERS_TO_READ)")

            // read one character into the collector
            val res = reader.read(strCollector, index, 1)
            if (res != 1) { // we've reached the end apparently
                break
            }

            // if we find a ',', add the collected string
            if (strCollector[index] == ',' || strCollector[index] == LINE_SEPARATOR) {
                parsedQuote = false
                if (inString) {
                    continue // skip other operations if we are in a string, then we just want to add it to the string.
                }

                strCollector[index] = '\u0000' // remove the last ',' or '\n' from the result.
                colCollector += String(strCollector) // add collected str
                strCollector.fill('\u0000') // clear collector
                index = -1 // reset index (will get incremented to 0 again :))

                if (colCollector.size >= CSV_NUM_COLS) {
                    // add row to result
                    result += convertRowToSubmissionRow(colCollector)
                    colCollector.clear() // clear row
                }
            } else if (strCollector[index] == '"') {
                inString = !inString // this also handles escaped quotes `""` as it just flips twice :)

                if (parsedQuote) // if the previous character is a quote, that means
                    continue // this is a double quote, so don't remove this one!

                // otherwise, remove this quote (it must be a single quote, which we do not want to keep)
                parsedQuote = true
                strCollector[index] = '\u0000' // remove the quote (we don't want it
                index-- // reset buffer back to previous index
            } else {
                parsedQuote = false
            }
        }

        return result
    }

    @Suppress("UNUSED_VARIABLE")
    /* want to make it clear which format we expect. */
    @Throws(IllegalStateException::class)
    private fun convertRowToSubmissionRow(rowCollector: MutableList<String>): Submission {
        if (rowCollector.size != 19)
            error("ROW DID NOT CONTAIN THE RIGHT NUM. OF ELEMENTS TO SATISFY FORMAT:\n\t${rowCollector.joinToString(",")}")

        val (resultID, candidateID, sectionTitle,
            questionNumber, questionID, questionVersion, questionCode, questionType,
            variableType, interactionNumber, variableName, variableInterpretation,
            variableMin, variableMax, variableRespType, variableCardinality, variableDefaultValue, variableValue, choiceSequence)
                = rowCollector
        try {
            return Submission(
                resultID.trim().trimNulls().toInt(),
                candidateID.trim().trimNulls().toInt(),
                questionNumber.trim().trimNulls().toInt(),
                questionID.trim().trimNulls().toInt(),
                questionCode.trim().trimNulls(),
                interactionNumber.trim().trimNulls().toInt(),
                variableName.trim().trimNulls(),
                variableValue.trim().trimNulls()
            )
        } catch (e: NumberFormatException) {
            error("Failed to format submission row: $e")
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val inputStr =
            FileInputStream("/home/douwe/Documents/UNIVERSITEIT_TWENTE/YEAR 3/MOD12-Research_Project/DATASET/2023-10-23 submissions.csv")
        val res = parseSubmissions(inputStr)
        println(res)
    }
}

private fun String.trimNulls(): String {
    return this.substringBefore("\u0000") // String.toInt() doesn't handle multiple null terminators well :(
}


private operator fun <E> List<E>.component6(): E {
    return this[5]
}

private operator fun <E> List<E>.component7(): E {
    return this[6]
}

private operator fun <E> List<E>.component8(): E {
    return this[7]
}

private operator fun <E> List<E>.component9(): E {
    return this[8]
}

private operator fun <E> List<E>.component10(): E {
    return this[9]
}

private operator fun <E> List<E>.component11(): E {
    return this[10]
}

private operator fun <E> List<E>.component12(): E {
    return this[11]
}

private operator fun <E> List<E>.component13(): E {
    return this[12]
}

private operator fun <E> List<E>.component14(): E {
    return this[13]
}

private operator fun <E> List<E>.component15(): E {
    return this[14]
}

private operator fun <E> List<E>.component16(): E {
    return this[15]
}

private operator fun <E> List<E>.component17(): E {
    return this[16]
}

private operator fun <E> List<E>.component18(): E {
    return this[17]
}

private operator fun <E> List<E>.component19(): E {
    return this[18]
}

