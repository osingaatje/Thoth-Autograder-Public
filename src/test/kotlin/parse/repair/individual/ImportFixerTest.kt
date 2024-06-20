package parse.repair.individual

import com.github.javaparser.ast.CompilationUnit
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.example.parse.repair.Constants
import org.example.parse.repair.fixers.ImportFixer
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assumptions.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import util.tryParseClassSubmission
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ImportFixerTest {
    @Test
    fun testAddImportsToNoImportsFile() {
        val tree = tryParseClassSubmission("""
            class Aaa {
                int a = 0;
                private final int b = 1;
                
                boolean abc(boolean test, String test2) {
                    if (test2.equals("TeStTtT"))
                        return test;
                       
                    return !test;
                }
            }
        """.trimIndent())

        val formattedTree = ImportFixer.formatSubmission(tree)
        assertNotNull(formattedTree.compilationUnit)
        val cu = formattedTree.compilationUnit!!
        // the imports should be only those in the ADD_IMPORTS_IF_NOT_EXISTS list
        assertEquals(Constants.ADD_IMPORTS_IF_NOT_EXIST, cu.imports.map { it.name.asString() })
    }

    @Test
    fun testRemoveOtherImports() {
        val importsThatWillBeRemoved : ImmutableList<String> = persistentListOf(
            "testImportThatDoesNotExist", "BobDeBouwer.NouEnOf"
        )
        importsThatWillBeRemoved.forEach {
            assertFalse(Constants.IGNORE_IMPORTS.contains(it)) // all imports to be removed should obviously not be ignored
        }

        val importString : String = importsThatWillBeRemoved.joinToString("\n") { "import $it;" } + "\n"
        val tree = tryParseClassSubmission("""
            $importString
            class SomeOtherClassName {
                void testFunc() {
                    System.out.println("aAAAAAaAAAA test"); 
                }
                
                int addOne(int input) {
                    return input + 1;
                }
            
            }
        """.trimIndent())

        val formattedTree = ImportFixer.formatSubmission(tree).compilationUnit ?: error("Parsed submission was not CU???")
        assertEquals(Constants.ADD_IMPORTS_IF_NOT_EXIST, formattedTree.imports.map { it.name.asString() })
    }

    @Test
    fun testIgnoreImports() {
        assumeFalse(Constants.IGNORE_IMPORTS.isEmpty(), "IGNORE IMPORTS list is empty, test case invalid")

        val tree = tryParseClassSubmission("""
            import ${Constants.IGNORE_IMPORTS[0]};
            
            public class AClass {
                int anInt = 69;
                bool aBool = true;
            }
        """.trimIndent())

        val formattedTree = ImportFixer.formatSubmission(tree).compilationUnit ?: error("Parsed submission wasn't a CompilationUnit? wtf?")

        val formattedTreeImportNames = formattedTree.imports.map { it.nameAsString }
        Constants.ADD_IMPORTS_IF_NOT_EXIST.forEach { addImportName ->
            assertTrue(formattedTreeImportNames.contains(addImportName))
        }
        assertTrue(formattedTreeImportNames.contains(Constants.IGNORE_IMPORTS[0]))
        assertEquals(Constants.ADD_IMPORTS_IF_NOT_EXIST.size + 1 /* from the ignores */, formattedTree.imports.size)
    }
}