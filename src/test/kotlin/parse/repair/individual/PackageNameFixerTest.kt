package parse.repair.individual

import com.github.javaparser.ast.CompilationUnit
import org.example.parse.repair.Constants
import org.example.parse.repair.fixers.PackageNameFixer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import util.tryParseClassSubmission

class PackageNameFixerTest {
    @Test
    fun testLackingPackageNameWithClass() {
        val tree = tryParseClassSubmission("""
            package JAVA;
            
            class TestingClass {
                int x = 0;
                private static final String y = "Yes!";
                
                int getX() {
                    return x;
                }
            }
        """.trimIndent())

        val modifiedTree = PackageNameFixer.formatSubmission(tree)
        assertNotNull(modifiedTree.compilationUnit)
        val cu = modifiedTree.compilationUnit!!
        assertEquals(Constants.SUBMISSION_PACKAGE_NAME, cu.packageDeclaration.get().nameAsString)
    }

    @Test
    fun testDifferentPackageNameWithClass() {
        val tree = tryParseClassSubmission("""
            class TestingClass {
                int x = 0;
                private static final String y = "Yes!";
                
                int getX() {
                    return x;
                }
            }
        """.trimIndent())

        val modifiedTree = PackageNameFixer.formatSubmission(tree)
        assertNotNull(modifiedTree.compilationUnit)
        val cu = modifiedTree.compilationUnit!!
        assertEquals(Constants.SUBMISSION_PACKAGE_NAME, cu.packageDeclaration.get().nameAsString)
    }
}