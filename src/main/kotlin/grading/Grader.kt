package org.example.grading

import org.example.analysis.dynamic_analysis.DynamicAnalyser
import org.example.grading.scheme.*
import analysis.static_analysis.StaticAnalyser
import org.example.util.compiler.CompilationException
import java.lang.reflect.InvocationTargetException
import org.example.grading.scheme.DynamicTestErrorCode.*
import org.example.parse.ParsedSubmission

object Grader {
    private const val PRINT_SUBMISSION_ERRORS = false
    private fun printDynamicErrSubmissionStr(testsuiteId : Int, candidateId: Int, exceptionStr : String) {
        if (PRINT_SUBMISSION_ERRORS)
            System.err.println("Error while testing dynamic test suite (id=${testsuiteId}) for Candidate ID $candidateId: $exceptionStr")
    }

    /**
     * Runs the SubmissionRunner for a particular student exercise of a particular exam and returns the results given by the SubmissionRunner.
     * @param submission The original submission from the student
     * @param formattedSubmission The submission of the student formatted to place it in the correct class etc. for dynamic testing
     * @return Map from exercise ID to exam result.
     */
    fun analyseSubmission(
        config: ExamConfig,
        exerciseNum: Int,
        submission: ParsedSubmission,
        formattedSubmission: ParsedSubmission,
        candidateId: Int
    ): ExamExerciseResult {
        val examExercise: ExamExercise = config.exercises.find { it.id == exerciseNum }
            ?: error(
                "Could not find exam exercise $exerciseNum in config with exercise ids [${
                    config.exercises.map { it.id }.joinToString(",")
                }]"
            )

        return analyseExercise(config, examExercise, submission, formattedSubmission, candidateId)
    }

    /**
     * Grades an `exercise` from the `config` and returns an ExamExerciseResult
     */
    private fun analyseExercise(
        config: ExamConfig,
        exercise: ExamExercise,
        submission: ParsedSubmission,
        formattedSubmission: ParsedSubmission,
        candidateId: Int
    ): ExamExerciseResult {
        val iloGrades: MutableList<ILOGrade> = mutableListOf()
        val submissionString = formattedSubmission.compilationUnit?.toString() ?: formattedSubmission.methodDeclaration?.toString() ?: error("Invalid Submission! No CU or MethodDeclaration present")

        for (iloWeight in exercise.iloWeights) {
            iloGrades += analyseILOWeight(config, iloWeight, submission, submissionString, candidateId)
        }
        val achievedPoints: Float = iloGrades.fold(0f) { acc, iloGrade -> acc + iloGrade.achievedPoints }
        return ExamExerciseResult(exercise.id, candidateId, achievedPoints, iloGrades)
    }

    private fun analyseILOWeight(
        config: ExamConfig,
        iloWeight: ILOWeight,
        submission: ParsedSubmission,
        submissionString: String,
        candidateId: Int
    ): ILOGrade {
        val dynamicTestSuiteGrades: MutableList<DynamicTestSuiteGrade> = mutableListOf()
        val staticTestSuiteGrades: MutableList<StaticTestSuiteGrade> = mutableListOf()

        for (dynamicTestSuite in iloWeight.dynamicTestSuites) {
            try {
                dynamicTestSuiteGrades += DynamicAnalyser.analyseDynamicTestSuite(dynamicTestSuite, submissionString)
            } catch (e: Exception) {
                var errorCode : DynamicTestError?
                when (e) {
                    is CompilationException -> {
                        val errorStr : String = e.toString()
                        printDynamicErrSubmissionStr(dynamicTestSuite.id, candidateId, errorStr)
                        errorCode = DynamicTestError(COMPILE_FAILURE, errorStr)
                    }
                    is NoSuchMethodException -> {
                        val errorStr = "Could possibly not find method ${dynamicTestSuite.funcName} in submission, DynamicAnalyser threw exception: ${e}"
                        printDynamicErrSubmissionStr(dynamicTestSuite.id, candidateId, errorStr)
                        errorCode = DynamicTestError(METHOD_NOT_FOUND, errorStr)
                    }
                    is InvocationTargetException -> {
                        val errorStr = "$e (${e.cause})"
                        printDynamicErrSubmissionStr(dynamicTestSuite.id, candidateId, errorStr)
                        errorCode = DynamicTestError(UNEXPECTED_SUBMISSION_EXCEPTION, errorStr)
                    }
                    else -> {
                        val errorStr : String = e.toString()
                        printDynamicErrSubmissionStr(dynamicTestSuite.id, candidateId, errorStr)
                        errorCode = DynamicTestError(UNEXPECTED_EXCEPTION, errorStr)
                    }
                }
                dynamicTestSuiteGrades += DynamicTestSuiteGrade(
                    dynamicTestSuite.id,
                    false,
                    0f,
                    errorCode.copy(errorString = errorCode.errorString.replace("[\n\t]".toRegex(), " ")),
                    listOf()
                )
            }
        }
        for (staticTestSuite in iloWeight.staticTestSuites) {
            staticTestSuiteGrades += StaticAnalyser.analyseStaticTestSuite(staticTestSuite, submission)
        }

        val totalPoints: Float = dynamicTestSuiteGrades.fold(0f) { acc, dynSuiteGrade ->
            acc + dynSuiteGrade.achievedPoints
        } + staticTestSuiteGrades.fold(0f) { acc, staticTestSuiteGrade -> acc + staticTestSuiteGrade.achievedPoints }

        return ILOGrade(iloWeight.id, totalPoints, dynamicTestSuiteGrades, staticTestSuiteGrades)
    }
}