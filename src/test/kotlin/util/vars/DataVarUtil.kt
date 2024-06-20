package util.vars

import grading.scheme.enums.KnowledgeKind
import grading.scheme.enums.KnowledgeLevel
import grading.scheme.enums.StaticCriterionType
import org.example.grading.scheme.*
import org.example.grading.scheme.vars.*
import org.example.grading.scheme.vars.PrimitiveType.*
import org.example.grading.scheme.vars.ComplexType.*
import util.random.RandUtil.getRandomString
import kotlin.random.Random

object DataVarUtil {
    fun getRandomComplexType() : ComplexType {
        return ComplexType.entries.random()
    }
    fun getRandomPrimitiveType(): PrimitiveType {
        return PrimitiveType.entries.filter { it != ANY }.random()
    }

    fun getRandomValueForType(type: PrimitiveType) : Any {
        return when(type) {
            BOOL -> Random.nextBoolean()
            NUM -> Random.nextInt(-10, 10)
            DECIMAL -> (Random.nextDouble((-10).toDouble(), 10.toDouble()) * 100).toInt().toDouble() / 100
            STR -> {
                getRandomString(1, 10)
            }

            ANY -> error("Cannot get random value for ANY datatype")
        }
    }

    fun getRandomTypeString() : Pair<String, DataVar> {
        val type = getRandomType()
        return Pair(DataVarSerializer.serialize(type), type)
    }

    fun getRandomType() : DataVar = if (Random.nextBoolean())
        PrimitiveVar(getRandomPrimitiveType())
    else {
        val complexType = ComplexType.entries.random()
        val primitiveTypes = (1..complexType.numArgTypes()).map { getRandomPrimitiveType() }
        ComplexVar(complexType, primitiveTypes.map { PrimitiveVar(it) })
    }

    fun getRandomValueForVar(dataType : DataVar?) : ValNode {
        val forType = dataType ?: getRandomType()
        when (forType) {
            is ComplexVar -> {
                when(forType.type) {
                    LIST -> {
                        val childVar = forType.args[0]
                        return if (childVar is PrimitiveVar)
                            ValNode(forType, (1..Random.nextInt(10)).map { ValLeaf(childVar, getRandomValueForType(childVar.type)) })
                        else
                            ValNode(forType, (1..Random.nextInt(10)).map { ValNode(childVar, (1..Random.nextInt(10)).map { getRandomValueForVar(childVar) }) })
                    }
                    MAP -> {
                        // generate two lists, with in these lists a set of primitives
                        val lists : MutableList<List<ValNode>> = mutableListOf()

                        val mapSize = Random.nextInt(10)
                        for (i in 0..1) {
                            val childType : DataVar = forType.args[i]

                            if (childType is PrimitiveVar) {
                                val childList : List<ValNode> =
                                    (1..mapSize).map {
                                        getRandomValueForVar(childType)
                                    }
                                lists += childList
                            } else {
                                val complexChild = childType as ComplexVar
                                val childList : List<ValNode> = (1..mapSize).map {
                                    ValNode(complexChild, (0 until complexChild.type.numArgTypes()).map { i ->
                                        getRandomValueForVar(complexChild.args[i])
                                    })
                                }
                                lists += childList
                            }
                        }
                        return ValNode(forType, listOf(
                            // LIST<?> type 1:
                            ValNode(ComplexVar(LIST, listOf(forType.args[0])), lists[0]),
                            ValNode(ComplexVar(LIST, listOf(forType.args[1])), lists[1])
                        ))
                    }
                }
            }
            is PrimitiveVar -> {
                return ValLeaf(forType, getRandomValueForType(forType.type))
            }
        }
        error("???")
    }

    fun getRandomDynamicTestCasesForType(inputType : DataVar, outputType: DataVar) : List<DynamicTestCase> {
        return (1..Random.nextInt(10)).map {
            DynamicTestCase(
                Random.nextInt(),
                listOf(ValNodeSerializer.serialize(getRandomValueForVar(inputType))), // TODO fix by adding more than one input type?
                ValNodeSerializer.serialize(getRandomValueForVar(outputType))
            )
        }
    }

    fun getRandomDynamicTestSuite() : DynamicTestSuite {
        val inputType = getRandomType()
        val outputType = getRandomType()
        return DynamicTestSuite(
            Random.nextInt(),
            Random.nextFloat(),
            "DynTestSuiteDescr:${getRandomString()}",
            listOf(DataVarSerializer.serialize(inputType)), DataVarSerializer.serialize(outputType),
            "Func:${getRandomString()}",
            Random.nextBoolean(),
            getRandomDynamicTestCasesForType(inputType, outputType))
    }

    fun generateRandomConfigForStaticCriterion() : Map<String, List<String>> {
        return (1..Random.nextInt(1, 5)).map {
            "CritConfigStr${getRandomString()}" to (1..Random.nextInt(5)).map { "ConfArg${getRandomString()}" }
        }.toMap()
    }

    fun getRandomStaticCriterion() : StaticCriterion {
        return StaticCriterion(Random.nextInt(), StaticCriterionType.entries.random(), generateRandomConfigForStaticCriterion())
    }

    fun getRandomStaticTestSuite() : StaticTestSuite {

        return StaticTestSuite(
            Random.nextInt(),
            Random.nextFloat() * 10,
            "StatTestSuiteDescr:${getRandomString()}",
            (1..Random.nextInt(1,3)).map { getRandomStaticCriterion() }
        )
    }

    fun getRandomILOType() = ILOType(
        Random.nextInt(),
        "IloTypeDescr: ${getRandomString(1, 10)}",
        KnowledgeLevel.entries.random(),
        KnowledgeKind.entries.random()
    )

    fun getRandomILOWeight() : ILOWeight {
        return getRandomILOWeight(null)
    }
    fun getRandomILOWeight(iloTypeIdOptions : List<Int>?) : ILOWeight {
        return ILOWeight(
            Random.nextInt(),
            if (iloTypeIdOptions?.isNotEmpty() == true) iloTypeIdOptions.random() else Random.nextInt(), // TODO possibly fix "All ilotype ids should correspond to valid ilo type" for generated ilos
            Random.nextFloat() * 10,
            (1..Random.nextInt(1, 3)).map { getRandomDynamicTestSuite() },
            (1..Random.nextInt(1, 3)).map { getRandomStaticTestSuite() })
    }

    fun getRandomExamExercise() : ExamExercise {
        return getRandomExamExercise(null)
    }
    fun getRandomExamExercise(iloTypeIdOptions : List<Int>?) : ExamExercise {
        return ExamExercise(
            Random.nextInt(),
            "ExamEx.Descr:${getRandomString()}",
            (1..Random.nextInt(1, 10)).map { getRandomILOWeight(iloTypeIdOptions) }
        )
    }

    fun getRandomExamConfig() : ExamConfig {
        val randomILOTypes = (1..Random.nextInt(10)).map { getRandomILOType() }
        val iloIDs = randomILOTypes.map { it.id }
        return ExamConfig(
            randomILOTypes,
            (1..Random.nextInt(5)).map { getRandomExamExercise(iloIDs) }
        )
    }
}