package org.example.grading.scheme

import kotlinx.serialization.Serializable

/*

                Exam Result (points: summed up from ilo grades)
                    | 1
                    | *
                ILOGrade (points: summed up from suites)
          /------/  1 \---------\
          |*                    |*
 DynamicSuiteGrade      StaticSuiteGrade (passed: decided based on testcase/criterion)
          |1                    |1
          |*                    |*
 DynamicTestCaseGrade    StaticCriterionGrade (binary passed/failed)
 */


@Serializable
data class ExamExerciseResult(val exerciseId: Int, val candidateId: Int, val achievedPoints: Float, val iloPoints: List<ILOGrade>)

@Serializable  // total points can be found in the grading config.
data class ILOGrade(val iloWeightId: Int, val achievedPoints: Float, val dynamicSuitePoints : List<DynamicTestSuiteGrade>, val staticSuitePoints : List<StaticTestSuiteGrade>)


// DYNAMIC
@Serializable
data class DynamicTestSuiteGrade(val suiteId : Int, val passed: Boolean, val achievedPoints: Float, val error: DynamicTestError?, val testCaseGrades : List<DynamicTestCaseGrade>)
@Serializable
data class DynamicTestError(val errorCode: DynamicTestErrorCode, val errorString: String)
enum class DynamicTestErrorCode {
    UNEXPECTED_EXCEPTION,
    COMPILE_FAILURE,
    UNEXPECTED_SUBMISSION_EXCEPTION,
    METHOD_NOT_FOUND
}

@Serializable
data class DynamicTestCaseGrade(val testcaseId : Int, val actualOutput : String, val passed : Boolean)


// STATIC
@Serializable
data class StaticTestSuiteGrade(val suiteId : Int, val achievedPoints: Float, val criterionGrades : List<StaticCriterionGrade>)

@Serializable
data class StaticCriterionGrade(val criterionId : Int, val passed: Boolean, val message : String?) {
    constructor(criterionId: Int, passed: Boolean) : this(criterionId, passed, null)
}

