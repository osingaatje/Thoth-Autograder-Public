package analysis.static

import grading.scheme.enums.StaticCriterionType
import org.example.analysis.static_analysis.criteria_verification.UsesFunctionVerifier
import org.example.grading.scheme.StaticCriterion
import org.example.parse.ParsedSubmission
import org.example.parse.SubmissionParserJava
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.test.fail

class FuncVisitorTest {
    private fun createCriterion(config: Map<String, List<String>>) =
        StaticCriterion(1, StaticCriterionType.USES_FUNCTION, config)

    private fun parseSubmission(submission: String): ParsedSubmission {
        return SubmissionParserJava.parseSubmission(submission) ?: fail("Could not parse input submission")
    }

    private fun runVerifierOnSubmission(config: Map<String, List<String>>, submission: String): Pair<Boolean, Any?> {
        val parsedSubmission = parseSubmission(submission)
        val criterion = createCriterion(config)
        val result = UsesFunctionVerifier.verifyCriterion(criterion, parsedSubmission)

        if (result.second !is Map<*, *>)
            fail("Result was not a map")

        @Suppress("UNCHECKED_CAST")
        return result as Pair<Boolean, Map<String, List<String>>>
    }

    @Test
    fun testBasicObjectFunCalls() {
        val res = runVerifierOnSubmission(
            mapOf(
                "funcNames" to listOf("isEmpty", "size", "keySet"),
                "selectionMethod" to listOf("ALL")
            ), """
            void testCallFunctions() {
                Map<String, String> map1 = new HashMap<>();
                
                map1.isEmpty();
                map1.size();
                map1.keySet();
            }
        """.trimIndent()
        )

        assertTrue(res.second is Map<*, *>)
        assertEquals(1, (res.second as Map<*, *>).size)
        assertEquals(listOf("isEmpty", "size", "keySet"), (res.second as Map<*, *>)["map1"])
    }

    @Test
    fun testInputVariableFunCallConfig() {
        val res = runVerifierOnSubmission(
            mapOf(
                "funcNames" to listOf("<ARG>.testFunctionCall", "someOtherFunCall", "toString"),
                "selectionMethod" to listOf("ALL")
            ), """
                int returnSumOfMap(HashMap<String, Integer> mapIn) {
                    mapIn.testFunctionCall();
                    mapIn.someOtherFunCall();
                    Integer x = 5;
                    x.toString();
                    return 1;
                }
                """.trimIndent()
        )

        assertTrue(res.first)
        assertTrue(res.second is Map<*,*>)
        assertTrue(((res.second as Map<*,*>)["<ARG>"] as List<*>).size == 2)
        assertTrue(((res.second as Map<*,*>)["x"] as List<*>).size == 1)
        println(res)
    }
    @Test
    fun testNotAllFunctionsInFunCallConfigAll() {
        val res = runVerifierOnSubmission(
            mapOf(
                "funcNames" to listOf("<ARG>.testFunctionCall", "someOtherFunCall", "toString"),
                "selectionMethod" to listOf("ALL")
            ), """
                int returnSumOfMap(HashMap<String, Integer> mapIn) {
                    mapIn.testFunctionCall();
                    // missing someOtherFunCall()
                    Integer x = 5;
                    x.toString();
                    return 1;
                }
                """.trimIndent()
        )

        assertFalse(res.first)
        assertTrue(res.second is Map<*,*>)
        assertTrue(((res.second as Map<*,*>)["<ARG>"] as List<*>).size == 1)
        assertTrue(((res.second as Map<*,*>)["x"] as List<*>).size == 1)
        println(res)
    }


    @Test
    fun testInputVariableFunCallConfigAny() {
        val res = runVerifierOnSubmission(
            mapOf(
                "funcNames" to listOf("<ARG>.someFunc", "<ARG>.someOtherFunc"),
                "selectionMethod" to listOf("ANY")
            ), """
                void doSomethingIGuess(HashMap<String, Integer> someMap) {
                    Integer x = 1;
                    someMap.someFunc();
                    x += 2;
                    x.equals(3);
                }
                """.trimIndent()
        )

        assertTrue(res.first)
        assertTrue(res.second is Map<*,*>)
        assertTrue(((res.second as Map<*,*>)["<ARG>"] as List<*>).size == 1)
        assertTrue(((res.second as Map<*,*>)["x"] as List<*>).size == 1)
        println(res)
    }
    @Test
    fun testNoAppropriateFunCallConfigAny() {
        val res = runVerifierOnSubmission(
            mapOf(
                "funcNames" to listOf("<ARG>.someWeirdFunction", "<ARG>.ehIdunnoman"),
                "selectionMethod" to listOf("ANY")
            ), """
                void doSomethingIGuess(HashMap<String, Integer> someMap) {
                    Integer x = 1;
                    someMap.normalFunc();
                    x += 2;
                    x.equals(3);
                }
                """.trimIndent()
        )

        assertFalse(res.first)
        assertTrue(res.second is Map<*,*>)
        assertTrue(((res.second as Map<*,*>)["<ARG>"] as List<*>).size == 1)
        assertTrue(((res.second as Map<*,*>)["x"] as List<*>).size == 1)
        println(res)
    }

}