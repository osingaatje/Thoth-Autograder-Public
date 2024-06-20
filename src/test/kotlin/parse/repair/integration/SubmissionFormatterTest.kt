package parse.repair.integration

import com.github.javaparser.ast.CompilationUnit
import org.example.parse.SubmissionParserJava
import org.example.parse.repair.Constants
import org.example.parse.repair.SubmissionFormatter
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assumptions.*
import org.junit.jupiter.api.BeforeEach
import util.tryParseClassSubmission
import util.tryParseFunctionSubmission

class SubmissionFormatterTest {
    companion object {
        const val PRINT_TREES : Boolean = true
    }

    private var formattedTree : CompilationUnit? = null
    private var checkFormattingOfTree : Boolean = true

    @BeforeEach
    fun prepare() {
        formattedTree = null
        checkFormattingOfTree = true
    }

    @AfterEach
    fun verifySubmission() {
        if (!checkFormattingOfTree)
            return

        assumeTrue(formattedTree != null, "Don't forget to assign formattedTree!") // ignore the test if we don't assign formattedTree

        if (PRINT_TREES)
            println(formattedTree)

        val tree : CompilationUnit = formattedTree!! // force tree to be non-null (to avoid stupid `?.property?.thing?.` calls

        // check package name
        assertEquals(Constants.SUBMISSION_PACKAGE_NAME, tree.packageDeclaration.get().name.asString())

        // check class name (WARNING: for now we only allow ONE class per submission) (FIXME: Change for OOP-type checking)
        assertEquals(1, tree.types.size)
        assertEquals(Constants.SUBMISSION_CLASS_NAME, tree.types[0].nameAsString)

        // check imports
        val importNames : List<String> = tree.imports.map { it.nameAsString }
        importNames.forEach {
            assertTrue(Constants.ADD_IMPORTS_IF_NOT_EXIST.contains(it) || Constants.IGNORE_IMPORTS.contains(it))
        }
    }

    @Test
    fun testFixPackageClassFunction() {
        val tree = tryParseClassSubmission("""
            package someweirdpackage.JAVA.orsomething;
            
            class AWeirdClassName {
                private bool x = true;
                public static final volatile Map<Int, Boolean> numbers;
                
                int someStudentFunctionName() {
                   return 6; 
                }
                
                @Override
                public String toString() {
                    return "Hey, I'm walkin' here!";
                }
            }
        """.trimIndent())

        formattedTree = SubmissionFormatter.formatSubmission(tree) as CompilationUnit
    }

    @Test
    fun testFixFunction() {
        // ignore this sub-par submission
        val tree = tryParseFunctionSubmission("""
            public static String notString(String s){
                String[] array = s.toCharArray();
                String result = "";
                if(array[0].toLowerCase() == "n" && array[1].toLowerCase() == "o" && array[2].toLowerCase() == "t"){
                    return s;
                }
                else{
                    return "not " + s;
                }
            }
        """.trimIndent())

        formattedTree = SubmissionFormatter.formatSubmission(tree) as CompilationUnit
    }

    @Test
    fun returnsNoneOnIncorrectSubmission() {
        val value = SubmissionParserJava.parseSubmission("haha balls")
        assertTrue(value == null)
        checkFormattingOfTree = false // skip format checking
    }
}