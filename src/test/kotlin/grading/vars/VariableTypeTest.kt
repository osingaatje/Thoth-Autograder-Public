package grading.vars

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.example.grading.scheme.vars.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

import org.example.grading.scheme.vars.PrimitiveType.*
import org.example.grading.scheme.vars.ComplexType.*
import kotlin.test.assertFailsWith

class VariableTypeTest {
    companion object {
        const val SHOW_CONSOLE_OUTPUT = true
    }

    // complex
    private fun list(type: DataVar) = ComplexVar(LIST, listOf(type))
    private fun map(type1: DataVar, type2: DataVar) = ComplexVar(MAP, listOf(type1, type2))
    // primitive
    private fun bool() = PrimitiveVar(BOOL)
    private fun num() = PrimitiveVar(NUM)
    private fun dec() = PrimitiveVar(DECIMAL)
    private fun str() = PrimitiveVar(STR)

    private fun serializeDataVar(dataVar: DataVar) = DataVarSerializer.serialize(dataVar)
    private fun deserializeDataVar(encodedDataVar: String) = DataVarSerializer.deserialize(encodedDataVar)

    @Test
    fun testPrimitiveVar() {
        for (primitiveType in PrimitiveType.entries) {
            val primitive = PrimitiveVar(primitiveType)
            val encodedPrimitive : String = serializeDataVar(primitive)
            val decodedPrimitive : PrimitiveVar = deserializeDataVar(encodedPrimitive) as PrimitiveVar

            assertEquals(primitiveType, decodedPrimitive.type, "PrimitiveVar encode-decode failed for $primitiveType")
        }
    }

    @Test
    fun testInvalidPrimitiveVar() {
        val testCases = listOf(
            "invalid",
            "boOl",
            "NUMBER",
            "PIPO DE CLOWN"
        )
        for (testCase in testCases) {
            assertThrows(IllegalArgumentException::class.java) {
                Json.decodeFromString<PrimitiveVar>("\"$testCase\"")
            }
        }
    }

    @Test
    fun test1DComplexVarTypes() {
        for (complexType in ComplexType.entries) {
            val args : MutableList<DataVar> = mutableListOf()
            for (i in 1..complexType.numArgTypes()) {
                args += PrimitiveVar(PrimitiveType.entries.random())
            }

            val complexVar = ComplexVar(complexType, args)

            // encode to string
            val encodedComplexVar : String = serializeDataVar(complexVar)
            if(SHOW_CONSOLE_OUTPUT)
                println("ENCODED COMPLEX 1D VAR: $encodedComplexVar")
            // decode from string
            val decodedComplexVar : ComplexVar = deserializeDataVar(encodedComplexVar) as ComplexVar

            assertEquals(complexType, decodedComplexVar.type, "Complex Type encode-decode failed for $complexType")
            assertEquals(complexVar, decodedComplexVar, "ComplexVar (type + children) encode-decode failed for $complexType")
        }
    }
    @Test
    fun testInvalidComplexVarArgumentCount() {
        val testCases = listOf(
            "LIST<BOOL, BOOL>",         // invalid argument count (2 instead of 1)
            "MAP<BOOL>",                // invalid argument count (1 instead of 2)
            "MAP<BOOL, BOOL, BOOL>",    // invalid argument count (3 instead of 2)
            "LIST",                     // no args
            "MAP"                       // no args 2
        )
        for (testCase in testCases) {
            println(assertThrows(IllegalArgumentException::class.java) {
                val variable = deserializeDataVar(testCase)
                println(variable)
            }.message)
        }
    }
    @Test
    fun testInvalidComplexArgumentSyntax() {
        val testCases = listOf(
            "LIST<BOOL",            // missing closing bracket
            "MAP<BOOL, BOOL",       // missing closing bracket
            "MIP MEP MAP<DinGUS>",  // weird syntax
            "MAP MAP BOOL INT"      // missing brackets in general
        )
        for (testCase in testCases) {
            println(assertFailsWith<IllegalArgumentException>("Expected IllegalArgumentException for $testCase") {
                deserializeDataVar(testCase)
            }.message)
        }
    }

    @Test
    fun test2DComplexVarTypes() {
        val testCases = listOf(
            list(list(bool())),         // LIST<LIST<BOOL>>
            map(list(num()), dec()),    // MAP<LIST<NUM>, STRING>
            map(list(str()), map(map(num(), bool()), str())), // MAP<LIST<STRING>, MAP<MAP<NUM, BOOL>, STRING>> (this should be more than complex enough to fulfill programming exam grading)
        )

        for (testCase in testCases) {
            val encodedComplexVar : String = serializeDataVar(testCase)
            if (SHOW_CONSOLE_OUTPUT)
                println("ENCODED COMPLEX 2D VAR: $encodedComplexVar")

            val decodedComplexVar : ComplexVar = deserializeDataVar(encodedComplexVar) as ComplexVar
            assertEquals(testCase, decodedComplexVar, "ComplexVar (type + children) encode-decode failed for $testCase")
        }
    }
}