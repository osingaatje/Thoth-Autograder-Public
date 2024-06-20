package org.example

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.example.grading.Grader
import org.example.grading.scheme.DynamicTestErrorCode
import org.example.grading.scheme.ExamConfig
import org.example.grading.scheme.ExamExerciseResult
import org.example.parse.SubmissionParserJava
import org.example.parse.repair.SubmissionFormatter
import org.example.util.commandline.*
import org.example.util.commandline.CommandType.*
import org.example.util.csv.parser.CSVParserRemindo
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.example.parse.ParsedSubmission
import org.example.util.MathUtil.roundPercent
import org.example.util.csv.formatter.CSVFormatter

const val helpMenu: String = """
========================== THOTH - HELP MENU ==========================
    OPTION   ARGS         | RANGE | EXPLANATION
    ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
 *  -s   <filepath>                 student submission file path
 *  -c   <filepath>                 grading configuration path
 *  -e   <exerciseNumber>   1-10    exercise to test
    -o   <dir>                      output directory to put results in 
    -p                              print the grading to the screen
    -h,  --help                     help menu
 
 * = required
=======================================================================
"""

@OptIn(ExperimentalSerializationApi::class)
fun main(args: Array<String>) {
    val logger = KotlinLogging.logger {}
    logger.info { "Initialising Thoth..." }

    val commandMap = Command.convertToCommandMap(args)
    if (commandMap.isEmpty() || commandMap.containsKey(HELP)) {
        logger.info { helpMenu }
        return
    }
    logger.info { "Commands: \n\t$commandMap\n" }


    // check optional printing and output file:
    if (!commandMap.containsKey(PRETTY_PRINT) && !commandMap.containsKey(OUTPUT_DIR_PATH)) {
        logger.error {
            "WARNING: No PrettyPrint (${PRETTY_PRINT.strOpts.joinToString(",")}}) or OutputDirectoryPath (${
                OUTPUT_DIR_PATH.strOpts.joinToString(
                    ","
                )
            }) specified. Results will not be printed to the screen or put in an output directory".replace("\n", "")
        }
    }

    // Checking required variables
    if (!commandMap.containsKey(SUBMISSION_FILE_PATH)) {
        logger.error { "Please input a submission file path (containing the exercise submissions of students)! (${SUBMISSION_FILE_PATH.strOpts})" }
        return
    }
    val submissionFilePath = FileInputStream((commandMap[SUBMISSION_FILE_PATH] as InputFileCommand).getPath())

    if (!commandMap.containsKey(EXERCISE_SELECTION)) {
        logger.error { "Please input an exercise number to check! (${EXERCISE_SELECTION.strOpts}" }
        return
    }
    val exerciseSelection: Int = (commandMap[EXERCISE_SELECTION] as ExerciseSelectionCommand).getSelection()

    if (!commandMap.containsKey(EXERCISE_CONFIG_FILE_PATH)) {
        logger.error { "Please input an exercise configuration file path! (${EXERCISE_CONFIG_FILE_PATH.strOpts})" }
        return
    }
    val gradingConfigStream =
        FileInputStream((commandMap[EXERCISE_CONFIG_FILE_PATH] as ExerciseConfigCommand).getPath())

    // get grading config
    val gradingConfig: ExamConfig = Json.decodeFromStream(gradingConfigStream)
    // verify grading config
    if (!gradingConfig.exercises.map { it.id }.contains(exerciseSelection)) {
        logger.error { "Your exercise selection (question $exerciseSelection) did not match any grading configuration exercises: (available: ${gradingConfig.exercises.map { it.id }})" }
        return
    }

    // get submissions
    var submissions = CSVParserRemindo.parseSubmissions(submissionFilePath)

    // FILTER OUT EMPTY CODE SUBMISSIONS
    val totalSubmissionCount = submissions.size
    val emptySubmissionCandidateIds = submissions.filter { it.variableValue.isBlank() }.map { it.candidateID }
    submissions = submissions.filter { it.variableValue.isNotBlank() }
    logger.info { "Removed a total of ${totalSubmissionCount - submissions.size} empty submissions out of $totalSubmissionCount submissions (empty submissions for candidate IDs $emptySubmissionCandidateIds)" }

    // FILTER SUBMISSIONS BASED ON EXERCISE NUMBER
    if (commandMap.containsKey(EXERCISE_SELECTION)) {
        submissions =
            submissions.filter { it.questionNumber == exerciseSelection }
                .toMutableList()
    }
    val submissionCountForExercise = submissions.size

    // Parse submissions
    val parsedSubmissions: MutableMap<Int, ParsedSubmission?> = submissions.associate {
        it.candidateID to SubmissionParserJava.parseSubmission(it.variableValue)
    }.toMutableMap()

    // Filter non-parsing submissions
    val parsedSubsNotNull: Map<Int, ParsedSubmission> =
        parsedSubmissions.filter { it.value != null }.map { it.key to it.value!! }.toMap()
    val parsedSubSize = parsedSubsNotNull.size
    logger.info {
        "Successfully parsed $parsedSubSize out of the total $submissionCountForExercise submissions (${
            roundPercent(parsedSubSize, submissionCountForExercise)
        }%)"
    }
    logger.warn { "Parsing failed for candidate IDs: ${parsedSubmissions.filter { it.value == null }.map { it.key }}" }

    // Prepare submissions for dynamic testing by replacing class names, placing functions in classes, etc.
    val formattedSubmissions: Map<Int, ParsedSubmission> = parsedSubsNotNull.map {
        it.key to SubmissionFormatter.formatSubmission(it.value)
    }.toMap()


//    // DEMO STEP: try to compile submissions
//    val compiledSubmissions: MutableMap<Int, Class<*>> = mutableMapOf() // map from Candidate ID to code
//    val compileFailures : MutableMap<Int, Exception> = mutableMapOf()
//    for ((candidateID, submission) in formattedSubmissions) {
//        try {
//            compiledSubmissions[candidateID] =
//                InMemoryJavaCompiler().compile("${SUBMISSION_PACKAGE_NAME}.${SUBMISSION_CLASS_NAME}", submission.toString())
//        } catch (e: Exception) {
//            compileFailures[candidateID] = e
//        }
//    }
//    println("Successfully compiled ${compiledSubmissions.size} out of ${formattedSubmissions.size} formatted submissions (${MathUtil.roundPercent(compiledSubmissions.size, formattedSubmissions.size)}%)")
//    compileFailures.forEach { errPrintln("Failed to compile for ID ${it.key}: ${it.value}") }

    // Time to grade!
    logger.info { "Starting to grade..." }

    val grades: MutableList<ExamExerciseResult> = mutableListOf()
    for ((candidateId, formattedSubmission) in formattedSubmissions) {
        grades += Grader.analyseSubmission(
            gradingConfig,
            exerciseSelection,
            parsedSubsNotNull[candidateId]!!,
            formattedSubmission,
            candidateId
        )
    }

    val failureTypeCounts: Map<DynamicTestErrorCode, Int> = DynamicTestErrorCode.entries.associateWith { errCode ->
        grades.count { grade -> grade.iloPoints.any { iloPoint -> iloPoint.dynamicSuitePoints.any { dsp -> dsp.error?.errorCode == errCode } } }
    }

    logger.info { "Successfully graded ${grades.size} out of $parsedSubSize parsed submissions!" }
    logger.warn {
        "Failed to grade ${parsedSubSize - grades.size} submissions. Reasons: \n\t\t${
            failureTypeCounts.map {
                String.format(
                    "%${DynamicTestErrorCode.entries.maxOfOrNull { e -> e.name.length }}s: %s",
                    it.key,
                    it.value
                )
            }.joinToString("\n\t")
        }\n"
    }

    if (commandMap.containsKey(PRETTY_PRINT)) {
        logger.info { "(not so) PRETTY PRINTED GRADES:\n\t\t" + grades.joinToString("\n\t\t") { "CandidateID: ${it.candidateId}, Points: ${it.achievedPoints}, Results: $it" } }
    }
    if (commandMap.containsKey(OUTPUT_DIR_PATH)) {
        val outputPath = (commandMap[OUTPUT_DIR_PATH] as OutputDirectoryPathCommand).getPath()
        logger.info { "Output directory specified! Preparing to write to dir \"$outputPath\"" }
        val dir = File(outputPath)
        // write output file to directory:
        if (!dir.isDirectory) {
            logger.error { "Please put in a valid directory as OutputDirPath (${OUTPUT_DIR_PATH.strOpts.joinToString(",")})" }
            return
        }

        // make files
        val currentDate = SimpleDateFormat("YYYY-MM-dd_HH-mm").format(Date())

        val jsonFileName = "${currentDate}_GRADED_SUBMISSIONS_exercise${exerciseSelection}_full_grades.json"
        val csvFileName  = "${currentDate}_GRADED_SUBMISSIONS_exercise${exerciseSelection}_summary.csv"

        val jsonOutputFile = File(dir, jsonFileName)
        val csvOutputFile = File(dir, csvFileName)

        logger.info { "Writing to path: $dir, fileName: $jsonFileName..." }
        // write to file:
        Json.encodeToStream(grades, jsonOutputFile.outputStream())
        logger.info { "Done writing JSON file!" }

        logger.info { "Writing to path: $dir, fileName: $csvFileName..." }
        csvOutputFile.writeText(CSVFormatter.toCSV(grades), Charsets.UTF_8)
    }
}
