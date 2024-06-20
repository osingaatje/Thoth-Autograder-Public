package util.compiler

import org.example.util.compiler.InMemoryJavaCompiler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class InMemoryCompilerTest {
    @Test
    fun testCompileMain() {
        val clazz : Class<*> = InMemoryJavaCompiler().compile("SomeClassName", """
            public class SomeClassName {
                public int countDigits(int number) {
                    String s = String.valueOf(number);
                    return s.length();
                }
            
                public static void main(String[] args) {
                    System.out.println("TEST PRINTING IN-MEMORY CODE!!!");
                }
            }
        """.trimIndent())

//        clazz.getMethod("main", Array<String>::class.java).invoke(null, arrayOf<String>())

        val classInstance = clazz.getConstructor().newInstance()
        val method = clazz.getMethod("countDigits", Int::class.java)

        val testCases : List<Pair<Int, Int>> = listOf(
            Pair(0, 1), Pair(9, 1), Pair(10, 2), Pair(11, 2), Pair(89, 2), Pair(99,2), Pair(100, 3), Pair(101, 3), Pair(999, 3), Pair(1_000, 4), Pair(9_999,4), Pair(10_000, 5), Pair(99_999, 5)
        )

        for ((input, expected) in testCases) {
            assertEquals(expected, method.invoke(classInstance, input))
        }
    }
}