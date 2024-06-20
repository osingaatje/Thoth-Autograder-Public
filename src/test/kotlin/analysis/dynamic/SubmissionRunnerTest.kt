package analysis.dynamic

import org.example.analysis.dynamic_analysis.SubmissionRunner
import org.junit.jupiter.api.Test

class SubmissionRunnerTest {
    @Test
    fun testFunctionSubmission() {
        val submissionClassName = "SomeClassName"
        val submissionCode = """
            public class SomeClassName {
                public int countDigits(int number) {
                    String s = String.valueOf(number);
                    return s.length();
                }
            }
        """.trimIndent()
        val submissionRunner = SubmissionRunner(submissionClassName).loadSubmission(submissionCode).constructClassInstance().loadMethod("countDigits", false, arrayOf(Int::class.java))


        val testCases : List<Pair<Int, Array<out Int>>> = listOf(
            Pair(1, arrayOf(1)), Pair(1, arrayOf(1)), Pair(9, arrayOf(1)), Pair(2, arrayOf(10)), Pair(2, arrayOf(99)), Pair(6, arrayOf(100_000)),
            Pair(6, arrayOf(999_999)), Pair(7, arrayOf(1_000_000)), Pair(7, arrayOf(9_999_999)), Pair(8, arrayOf(10_000_000)), Pair(8, arrayOf(78_910_112)), Pair(10, arrayOf(1_234_567_789))
        )
        val results = submissionRunner.assertEqualsMultiple(testCases)
        for (result in results) {
            println("RESULT: INPUT=${result.inputs}, OUTPUT:${result.actualOutput}, EXPECTED:${result.expectedOutput}, PASSED? ${result.passed}")
        }
    }
}