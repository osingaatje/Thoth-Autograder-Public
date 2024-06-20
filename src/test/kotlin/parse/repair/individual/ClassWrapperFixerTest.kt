package parse.repair.individual

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import org.example.parse.ParsedSubmission
import org.example.parse.SubmissionParserJava
import org.example.parse.repair.fixers.ClassWrapperFixer
import org.example.parse.repair.Constants
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.fail

class ClassWrapperFixerTest {
    companion object {
        const val PRINT_TREES = true
    }

    private fun tryParseSubmission(submission : String) : ParsedSubmission {
        val tree = SubmissionParserJava.parseSubmission(submission) ?: fail("Could not parse ")
        return tree
    }
    @Test
    fun testFunctionWrapping() {
        val tree = tryParseSubmission("""
            int testFunc(int addNum, String subString, char notUsed) {
                Boolean x = true;
                if (x) {
                    return 5 + addNum;
                } else if (subString.equalsIgnoreCase("test")) {
                    return 6;
                } else {
                    return 10;
                }
            }
        """.trimIndent())

        val formattedTree = ClassWrapperFixer.formatSubmission(tree)
        if (PRINT_TREES)
            println(formattedTree)

        Assertions.assertNotNull(formattedTree.compilationUnit)
        Assertions.assertNull(formattedTree.methodDeclaration)
        Assertions.assertEquals(
            Constants.SUBMISSION_CLASS_NAME,
            (formattedTree as ClassOrInterfaceDeclaration).name.asString()
        )

        val classNode = formattedTree as ClassOrInterfaceDeclaration
        // class should contain a MethodDeclaration
        Assertions.assertEquals(1, classNode.childNodes.filterIsInstance<MethodDeclaration>().size)

        val methodDecl = classNode.childNodes.filterIsInstance<MethodDeclaration>()[0]
        // check if all attributes are equal:
        Assertions.assertEquals(tree, methodDecl)
    }

    @Test
    fun testClassRenaming() {
        val tree = tryParseSubmission(
            """
            class SomeOtherClassNameThatWillBeRenamedLaterOn {
                public static final String x = "X!";
                private boolean y = false;
                
                boolean test1(String arg1, int arg2, int arg3) {
                    return arg2 == arg3 || arg1.equals("special arg!");
                }
                
                @Override
                int doSomething(int a) {
                    return a + 2;
                }
            }
        """.trimIndent()
        )
        val oldClassDef = tree.compilationUnit?.childNodes?.get(0) ?: error("Parsed thing was not a compilationunit or had no children?")

        val formattedTree = ClassWrapperFixer.formatSubmission(tree)
        if (PRINT_TREES)
            println(formattedTree)

        Assertions.assertNotNull(formattedTree.compilationUnit)
        val cu = formattedTree.compilationUnit!!
        val newClassDef = cu.getClassByName(Constants.SUBMISSION_CLASS_NAME)
        Assertions.assertNotNull(newClassDef)
        Assertions.assertEquals(
            oldClassDef.childNodes,
            cu.childNodes[0].childNodes
        ) // should check if everything about the classes is the same.
    }
}