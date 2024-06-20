package analysis.static

import analysis.static_analysis.criteria_verification.UsesControlStructureVerifier
import grading.scheme.enums.StaticCriterionType
import org.example.grading.scheme.StaticCriterion
import org.example.parse.ParsedSubmission
import org.example.parse.SubmissionParserJava
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.test.fail

class UsesControlStructureTest {
    private fun createCriterion(config: Map<String, List<String>>) = StaticCriterion(1, StaticCriterionType.USES_CONTROL_STRUCTURE, config)

    private fun parseSubmission(submission: String) : ParsedSubmission {
        return SubmissionParserJava.parseSubmission(submission) ?: fail("Could not parse input submission")
    }

    private fun runVerifierOnSubmission(config: Map<String, List<String>>, submission: String) : Pair<Boolean, Map<String, Int>> {
        val parsedSubmission = parseSubmission(submission)
        val criterion = createCriterion(config)
        val result = UsesControlStructureVerifier.verifyCriterion(criterion, parsedSubmission)

        if (result.second !is Map<*, *>)
            fail("Result was not a map ")

        @Suppress("UNCHECKED_CAST")
        return result as Pair<Boolean, Map<String, Int>>
    }

    @Test
    fun usesForLoopFunction() {
        val config = mapOf(
            "structures" to listOf("for")
        )

        val submission = """
            public static int doAForLoop() {
                int result = 0;
                for (int i = 0; i < 10; i++) {
                    result += i;
                }
                return result;
            }
        """.trimIndent()

        val result : Pair<Boolean, Map<String, Int>> = runVerifierOnSubmission(config, submission)
        assertTrue(result.first)
        assertEquals(mapOf("for" to 1), result.second)
    }

    @Test
    fun usesWhileLoopAndForLoopFunction() {
        val config = mapOf(
            "structures" to listOf("for", "while")
        )

        val submission = """
            public static int doAForLoop() {
                int result = 0;
                for (int i = 0; i < 10; i++) {
                    result += i;
                }
                while(result < 10) {
                    System.out.println("cookies or something");
                }
                return result;
            }
        """.trimIndent()

        val result : Pair<Boolean, Map<String, Int>> = runVerifierOnSubmission(config, submission)
        assertTrue(result.first)
        assertEquals(mapOf("for" to 1, "while" to 1), result.second)
    }
    @Test
    fun usesNestedWhileLoopAndForLoopFunction() {
        val config = mapOf(
            "structures" to listOf("for", "while")
        )

        val submission = """
            public static int doAForLoop() {
                int result = 0;
                for (int i = 0; i < 10; i++) {
                     while(result < 10) {
                        System.out.println("cookies or something");
                    }
                    result += i;
                }
               
                return result;
            }
        """.trimIndent()

        val result : Pair<Boolean, Map<String, Int>> = runVerifierOnSubmission(config, submission)
        assertTrue(result.first)
        assertEquals(mapOf("for" to 1, "while" to 1), result.second)
    }
    @Test
    fun usesDoWhile() {
        val config = mapOf(
            "structures" to listOf("do")
        )

        val submission = """
            public static int doAForLoop() {
                int result = 0;
                do {
                    System.out.println("cookies or something");
                    result += i;
                } while(result < 10);
               
                return result;
            }
        """.trimIndent()

        val result : Pair<Boolean, Map<String, Int>> = runVerifierOnSubmission(config, submission)
        assertTrue(result.first)
        assertEquals(mapOf("do" to 1), result.second)
    }

    @Test
    fun canVerifyThatSubmissionsDoNotUseControlMethods() {
        val config = mapOf(
            "structures" to listOf("while")
        )

        val submission = """
            public static int doAForLoop() {
                int result = 0;
                for (int i = 0; i < 69; i += 42) {
                    System.out.println("cookies or something");
                    result += i;
                }
               
                return result;
            }
        """.trimIndent()

        val result : Pair<Boolean, Map<String, Int>> = runVerifierOnSubmission(config, submission)
        assertFalse(result.first)
        assertEquals(mapOf("for" to 1), result.second)
    }
}