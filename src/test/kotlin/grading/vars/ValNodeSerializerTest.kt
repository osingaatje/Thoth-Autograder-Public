package grading.vars

import kotlinx.serialization.json.internal.decodeToSequenceByReader
import org.example.grading.scheme.vars.PrimitiveType.*
import org.example.grading.scheme.vars.ComplexType.*

import org.example.grading.scheme.vars.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import util.vars.DataVarUtil.getRandomComplexType
import util.vars.DataVarUtil.getRandomPrimitiveType
import util.vars.DataVarUtil.getRandomValueForType
import kotlin.random.Random

class ValNodeSerializerTest {
    private fun verifyEncodeDecodeValLeaf(leafNode : ValLeaf) {
        val encodedToStr : String = ValNodeSerializer.serialize(leafNode)
        println("ENCODED LEAF: $encodedToStr")
        val decoded : ValNode = ValNodeSerializer.deserialize(encodedToStr)
        assertTrue(decoded is ValLeaf)
        assertEquals(leafNode, decoded as ValLeaf)
    }
    private fun verifyEncodeDecodeValNode(valNode: ValNode) {
        val encodedToStr : String = ValNodeSerializer.serialize(valNode)
        println("ENCODED NODE: $encodedToStr")
        val decoded : ValNode = ValNodeSerializer.deserialize(encodedToStr)
        assertEquals(valNode.dt, decoded.dt)
        assertEquals(valNode.children, decoded.children)
        assertEquals(valNode, decoded) // finally, assert complete thing (to make sure that combination of dt and children is also equal)
    }

    // PRIMITIVE TYPES
    @RepeatedTest(1_000)
    fun testPrimitiveType() {
        // get random type
        val type : PrimitiveType = getRandomPrimitiveType()

        // get random value for leaf node
        val leafNode = ValLeaf(PrimitiveVar(type), getRandomValueForType(type))
        verifyEncodeDecodeValLeaf(leafNode)
    }

    @ParameterizedTest
    @ValueSource(strings = ["string[", "string]string", "(", ")", "str(str", "ano)ther()string", "strinblackslash\\", "stringdoublebckslsh\\\\"])
    fun correctlyEscapesCharacters(s: String) {
        val leafNode = ValLeaf(PrimitiveVar(STR), s)
        val str = ValNodeSerializer.serialize(leafNode)
        println("ESCAPED STR:$str")
        assertTrue(str.contains("\\")) // must only be escaped by two backslashes.

        val decodedVal : ValLeaf = ValNodeSerializer.deserialize(str) as ValLeaf
        println("Decoded: $decodedVal")
        assertEquals(leafNode, decodedVal)
    }

    @ParameterizedTest
    @ValueSource(strings = ["NameWith[Bracket", "NameWith]Bracket", "[", "]", "NamewithBoth[]Brackets", "[]", "Test![FLKSJDF]!$!@[]"])
    fun handlesBracketsInString(s : String) {
        val leafNode = ValLeaf(PrimitiveVar(STR), s)

        verifyEncodeDecodeValLeaf(leafNode)
    }

    @ParameterizedTest
    @ValueSource(strings = ["Name(Witha Brace", "AAAA( ( ( multiple???", "Check(one)two)three)", "FOUR()()()", "()"])
    fun handlesParensInStrings(s : String) {
        val leafNode = ValLeaf(PrimitiveVar(STR), s)
        verifyEncodeDecodeValLeaf(leafNode)
    }


    // COMPLEX TYPES
    @RepeatedTest(50)
    fun verifyOneDimensionalComplexes() {
        val complexType = ComplexType.entries.random()
        val primitiveTypes : List<PrimitiveVar> = (1 .. complexType.numArgTypes()).map { PrimitiveVar(getRandomPrimitiveType()) }

        val complexVar = ComplexVar(complexType, primitiveTypes)

        val valNode = ValNode(complexVar, primitiveTypes.map { ValLeaf(it, getRandomValueForType(it.type)) })
        verifyEncodeDecodeValNode(valNode)
    }

    @Test
    fun canSerialiseEmptyLists() {
        val emptyListStr = "LIST[]"
        val deserializedList = ValNodeSerializer.deserialize(emptyListStr)

        assertTrue(deserializedList.dt is ComplexVar)
        assertTrue((deserializedList.dt as ComplexVar).args.size == 1)
        assertTrue((deserializedList.dt as ComplexVar).args.all { it is PrimitiveVar && it.type == ANY })

        // assert that the kotlinValue is an empty list as well:
        val realValue = deserializedList.getKotlinValue()
        assertTrue(realValue is List<*>)
        assertTrue((realValue as List<*>).isEmpty())
    }

    @Test
    fun canSerialiseMaps() {
        // MAP<STR, INT> with 2 entries
        val mapStrNum = ComplexVar(MAP, listOf(PrimitiveVar(STR), PrimitiveVar(NUM)))
        val keys = ValNode(ComplexVar(LIST, listOf(PrimitiveVar(STR))), listOf(ValLeaf(PrimitiveVar(STR), "Key1"), ValLeaf(PrimitiveVar(STR), "Key2")))
        val values = ValNode(ComplexVar(LIST, listOf(PrimitiveVar(NUM))), listOf(ValLeaf(PrimitiveVar(NUM), 1), ValLeaf(PrimitiveVar(NUM), 2)))
        val map1 = ValNode(mapStrNum, listOf(keys, values))

        val serialisedMap1 = ValNodeSerializer.serialize(map1)
        println(serialisedMap1)
        val deserializedMap1 = ValNodeSerializer.deserialize(serialisedMap1)

        assertEquals(map1, deserializedMap1)

        // can (de)serialize empty map
        val keys2 = ValNode(ComplexVar(LIST, listOf(PrimitiveVar(STR))), listOf())
        val values2 = ValNode(ComplexVar(LIST, listOf(PrimitiveVar(NUM))), listOf())
        val map2 = ValNode(mapStrNum, listOf(keys2, values2))
        val serializedMap2 = ValNodeSerializer.serialize(map2)
        val deserializedMap2 = ValNodeSerializer.deserialize(serializedMap2)

        // could not infer type, so it's a MAP<ANY, ANY> (or MAP<?,?>)
        assertTrue(deserializedMap2.dt is ComplexVar)
        assertTrue((deserializedMap2.dt as ComplexVar).args.all { it is PrimitiveVar && it.type == ANY })

        val realValueMap1 = map1.getKotlinValue()
        assertTrue(realValueMap1 is HashMap<*,*>)
        assertTrue((realValueMap1 as HashMap<*,*>).size == 2)


        // finally, assert that the maps are equal (MAP<?,?> of size 0 should be equal to MAP<whatever, whatever> of size 0)
        assertEquals(map2, deserializedMap2)

        val realValue = map2.getKotlinValue()
        assertTrue(realValue is HashMap<*,*>)
        assertTrue((realValue as HashMap<*,*>).isEmpty())
    }

    @ParameterizedTest
    @ValueSource(strings = ["WME\\[A\\\\", "Test[]]][()", ""])
    fun handlesComplexTypesWithStringsContainingSpecialChars(s : String) {
        val complexVar = ComplexVar(LIST, listOf(PrimitiveVar(STR)))
        val valNode = ValNode(complexVar, listOf(ValLeaf(PrimitiveVar(STR), s)))

        verifyEncodeDecodeValNode(valNode)
    }

    private fun createRandomComplexListType(primType: PrimitiveType, length : Int?) : ValNode {
        return ValNode(ComplexVar(LIST, listOf(PrimitiveVar(primType))),
            createRandomListWithType(primType, length))
    }
    private fun createRandomListWithType(primType : PrimitiveType, length : Int?) : List<ValLeaf> {
        return (1..(length ?: Random.nextInt(5))).map { ValLeaf(PrimitiveVar(primType), getRandomValueForType(primType)) }
    }
    private fun getNDimensionalComplexVal(dimension: Int, cType: ComplexType?) : ValNode {
        when (dimension) {
            0 -> {
                val primType = getRandomPrimitiveType()
                return ValLeaf(PrimitiveVar(primType), getRandomValueForType(primType))
            }
            1 -> {
                val complexType = cType ?: getRandomComplexType()
                val randomPrimType = getRandomPrimitiveType()

                var valNode : ValNode? = null
                if (complexType == LIST) {
                    val randomPrimitiveList = (1..complexType.numArgTypes()).map { PrimitiveVar(randomPrimType) }
                    // default complex with primitives
                    valNode = ValNode(
                        ComplexVar(complexType, randomPrimitiveList),
                        randomPrimitiveList.map { ValLeaf(it, getRandomValueForType(it.type)) })
                } else if (complexType == MAP) {
                    val primType2 = getRandomPrimitiveType()
                    // a MAP needs two LISTs as children!
                    val len = Random.nextInt(5)

                    valNode = ValNode(
                        ComplexVar(
                            MAP,
                            listOf(PrimitiveVar(randomPrimType), PrimitiveVar(primType2))),
                        listOf(
                            createRandomComplexListType(randomPrimType, len),
                            createRandomComplexListType(primType2, len),
                        ))
                }

                return valNode!!
            }
            else -> {
                // complex with complexes
                val complexType = getRandomComplexType()

                val predefinedComplexType = if (complexType == MAP) LIST else getRandomComplexType()

                val children = (1..(if (complexType == MAP) 2 else Random.nextInt(1, 10))).map { getNDimensionalComplexVal(dimension - 1, predefinedComplexType) }
                return ValNode(ComplexVar(complexType, (if (complexType == LIST) listOf(children[0].dt) else children.map { it.dt })), children)
            }
        }
    }

    @RepeatedTest(5)
    fun verifyTwoDimensionalComplexes() {
        val complexVal = getNDimensionalComplexVal(2, null)
        verifyEncodeDecodeValNode(complexVal)
    }

    @Test
    fun hasConsistentHashCode() {
        val complexVar1 = ComplexVar(LIST, listOf(PrimitiveVar(DECIMAL)))
        val complexVar2 = ComplexVar(LIST, listOf(PrimitiveVar(DECIMAL)))
        assertEquals(complexVar1, complexVar2)

        val n1 = ValNode(complexVar1, listOf(ValLeaf(complexVar1.args[0] as PrimitiveVar, 9.3)))
        val n2 = ValNode(complexVar2, listOf(ValLeaf(complexVar2.args[0] as PrimitiveVar, 9.3)))
        assertEquals(n1, n2)

        val cv3 = ComplexVar(LIST, listOf(complexVar1))
        val n3 = ValNode(cv3, listOf(ValNode(complexVar1, listOf(n1))))
        val n4 = ValNode(cv3, listOf(ValNode(complexVar1, listOf(n1))))
        assertEquals(n3, n4)

        // one example did not pass automatic testing at first:
        //      "LIST[LIST[DEC(3.47)],LIST[DEC(9.7)]]"
        val valNode = ValNode(ComplexVar(LIST, listOf(ComplexVar(LIST, listOf(PrimitiveVar(DECIMAL))))),
            listOf(
                ValNode(ComplexVar(LIST, listOf(PrimitiveVar(DECIMAL))),
                    listOf(ValLeaf(PrimitiveVar(DECIMAL), 3.47))),

                ValNode(ComplexVar(LIST, listOf(PrimitiveVar(DECIMAL))),
                    listOf(ValLeaf(PrimitiveVar(DECIMAL), 9.7)))
                )
            )

        val str = ValNodeSerializer.serialize(valNode)
        val decoded1 = ValNodeSerializer.deserialize(str)
        val decoded2 = ValNodeSerializer.deserialize(str)

        assertEquals(valNode, decoded1)
        assertEquals(valNode, decoded2)
        assertEquals(decoded1, decoded2)
    }

    @Test
    fun canSerialiseNull() {
        val nullVal = ValNull()
        val encodedNull = ValNodeSerializer.serialize(nullVal)
        println("ENCODED: $encodedNull")
        val decodedNull : ValNode = ValNodeSerializer.deserialize(encodedNull)

        assertTrue(decodedNull is ValNull)
        assertEquals(nullVal, decodedNull)
    }
}