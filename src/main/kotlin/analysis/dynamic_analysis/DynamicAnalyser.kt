package org.example.analysis.dynamic_analysis

import org.example.analysis.dynamic_analysis.SubmissionRunner
import org.example.grading.scheme.DynamicTestCaseGrade
import org.example.grading.scheme.DynamicTestSuiteGrade
import org.example.grading.scheme.DynamicTestCase
import org.example.grading.scheme.DynamicTestSuite
import org.example.grading.scheme.vars.DataVar
import org.example.grading.scheme.vars.DataVarSerializer
import org.example.grading.scheme.vars.ValNodeSerializer
import org.example.parse.repair.Constants

object DynamicAnalyser {
    fun analyseDynamicTestSuite(
        testSuite: DynamicTestSuite,
        submission: String
    ): DynamicTestSuiteGrade {
        var inputTypes: List<DataVar> = listOf()
//        var outputType: DataVar? = null

        try {
            inputTypes = testSuite.inputTypes.map { DataVarSerializer.deserialize(it) }
//            outputType = DataVarSerializer.deserialize(testSuite.outputType) TODO: Check input/output types?
        } catch (e: IllegalArgumentException) {
            error("Could not deserialize input/output type(s) (in=${testSuite.inputTypes}, out=${testSuite.outputType})!")
        }

        val runner =
            SubmissionRunner("${Constants.SUBMISSION_PACKAGE_NAME}.${Constants.SUBMISSION_CLASS_NAME}").loadSubmission(
                submission
            ).loadMethod(
                testSuite.funcName,
                testSuite.funcIsStatic,
                inputTypes.map { it.getClassRepresentation() }.toTypedArray()
            )

        if (!testSuite.funcIsStatic)
            runner.constructClassInstance() // possible todo: add constructor args for classes?

        val testCaseResults: MutableList<DynamicTestCaseGrade> = mutableListOf()

        // test all test cases:
        for (testCase in testSuite.testCases) {
            val inputs: List<Any?> = testCase.inputs.map { ValNodeSerializer.deserialize(it).getKotlinValue() }

            if (testCase.expectedOutput != null) {
                // Expected output, should not throw exception
                val expectedOutput: Any? = ValNodeSerializer.deserialize(testCase.expectedOutput).getKotlinValue()
                try {
                    val res: DynamicTestCaseResult = runner.assertEquals(expectedOutput, *(inputs.toTypedArray()))
                    testCaseResults += DynamicTestCaseGrade(testCase.id, res.actualOutput.toString(), res.passed)
                } catch (e : Exception) {
                    testCaseResults += DynamicTestCaseGrade(testCase.id, "Unexpected exception while running test case ($testCase, input=$inputs, output=$expectedOutput): $e", false)
                }
            } else {
                // Test case with an expected exception:
                val exceptionType: String = testCase.exception
                    ?: error("Illegal test case: had a `null` expectedOutput AND a `null` exception. Please either enter the expected output or the except that is expected to be thrown")

                try {
                    val res = runner.assertEquals(null, inputs)
                    testCaseResults += DynamicTestCaseGrade(testCase.id, res.toString(), false)
                } catch (e : Exception) {
                    // verify exception type:
                    testCaseResults += if (e.javaClass.simpleName == exceptionType || exceptionType == "Exception" /* Allow teacher to specify "any" exception */) {
                        // test case succeeded!
                        DynamicTestCaseGrade(testCase.id, e.toString(), true)
                    } else {
                        // wrong exception thrown, failed!
                        DynamicTestCaseGrade(testCase.id, e.toString(), false)
                    }
                }
            }
        }


        val passed: Boolean = testCaseResults.all { it.passed }
        return DynamicTestSuiteGrade(testSuite.id, passed, if (passed) testSuite.points else 0f, null, testCaseResults)
    }


    @JvmStatic
    fun main(args: Array<String>) {
        val testCases: List<DynamicTestCase> = listOf(
            DynamicTestCase(1, listOf("NUM(0)"), "NUM(1)"),
            DynamicTestCase(2, listOf("NUM(1)"), "NUM(1)"),
            DynamicTestCase(3, listOf("NUM(10)"), "NUM(2)")
        )
        val testSuite =
            DynamicTestSuite(1, 1.0f, "aaaa", listOf("NUM"), "NUM", "countDigits", true, testCases)

        println(
            analyseDynamicTestSuite(
                testSuite, """
            package ${Constants.SUBMISSION_PACKAGE_NAME};
            
            class ${Constants.SUBMISSION_CLASS_NAME} {
            
                public static int countDigits(int input) {
                    return 1;
                }
            
            }
           """.trimIndent()
            )
        )
    }
}