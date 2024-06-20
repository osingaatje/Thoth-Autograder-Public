package parse

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.ExpressionStmt
import org.example.parse.ParsedSubmission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import util.*

class SubmissionParserJavaTest {
    @Test
    fun testFunctionSubmission() {
        val tree = tryParseFunctionSubmission("""
            void testFunction() {
                System.out.println("Test Function!");
            }
        """.trimIndent())

        val funcNode = tree.methodDeclaration ?: error("Parsed submission was not a method declaration?")
        assertFunctionCharacteristics(funcNode, "void", "testFunction")
        // has one child node (println statement) that prints "Test Function!"
        assertEquals(1, funcNode.body.get().statements.size)
        assertEquals("Test Function!", ((((funcNode.body.get().statements.get(0) as ExpressionStmt).expression as MethodCallExpr).arguments.get(0) as StringLiteralExpr).value))
    }

    @Test
    fun testClassSubmission() {
        val tree : ParsedSubmission = tryParseClassSubmission("""
            class TestClass {
                private int x = 5;
                public static final int y;
                
                public int doSomethingWithX(int addInt, String subString, char notUsed) {
                    if (subString.equalsIgnoreCase("test")) {
                        x += addInt;
                    }
                    return x;
                }
            }
        """.trimIndent())
        val cu = tree.compilationUnit ?: error("Parsed submission was not a CU?")

        val classNode : ClassOrInterfaceDeclaration = cu.findAll(ClassOrInterfaceDeclaration::class.java)[0]
        assertEquals("TestClass", classNode.name.asString())


        // check that the variables are correct
        val fieldDecls = classNode.findAll(FieldDeclaration::class.java)
        assertEquals(2, fieldDecls.size)
        assertFieldDeclCharacteristics(fieldDecls[0], listOf(Modifier.Keyword.PRIVATE), "x")
        var assignmentExpression : IntegerLiteralExpr? = null
        fieldDecls[0].variables[0].initializer.ifPresent { assignmentExpression = it.asIntegerLiteralExpr() }
        assertEquals("5", assignmentExpression?.value)

        assertFieldDeclCharacteristics(fieldDecls[1], listOf(Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC, Modifier.Keyword.FINAL), "y")
        // verify no initialise statement is present for "y"
        assertTrue(fieldDecls[1].variables[0].initializer.isEmpty)

        // check that function declarations are correct
        val funcDecls = classNode.findAll(MethodDeclaration::class.java)
        assertEquals(1, funcDecls.size)
        assertFunctionCharacteristics(funcDecls[0], "int", "doSomethingWithX")
    }
}