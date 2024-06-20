package org.example.util.csv.formatter

import org.example.grading.scheme.ExamExerciseResult

object CSVFormatter {
    private fun gradeToCSV(grade : ExamExerciseResult) : String {
        return "${grade.candidateId},${grade.exerciseId},${grade.achievedPoints}\n"
    }

    private fun getHeaderForExResult(): List<String> {
        return listOf(
            "CandidateID",
            "ExerciseNumber",
            "AchievedPoints"
        )
    }

    fun toCSV(grades : List<ExamExerciseResult>) : String {
        val cols = getHeaderForExResult()
        val result = StringBuilder(cols.joinToString(",")).append("\n")

        grades.forEach {
            result.append(gradeToCSV(it))
        }
        return result.toString()
    }
}