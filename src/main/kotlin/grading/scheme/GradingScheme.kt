package org.example.grading.scheme

import grading.scheme.enums.*
import kotlinx.serialization.Serializable

import grading.scheme.enums.KnowledgeKind.*
import grading.scheme.enums.KnowledgeLevel.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// TODO place this in config as well
object ModuleILOTypes {
    val ilo1 = ILOType(1, "Select and use appropriate primitive datatypes, including their preconceived behaviours (methods of such a type)", APPLYING, PROCEDURAL)
    val ilo2 = ILOType(2, "Develop statements for data transformations over primitive datatypes using the appropriate operators, including typecasting of primitive types (Java)", APPLYING, PROCEDURAL)
    val ilo3 = ILOType(3, "Express algorithmic solutions that use sequence and selection structures (conditionals) - (Java)", APPLYING, PROCEDURAL)
    val ilo4 = ILOType(4, "Express algorithmic solutions that use repetition structures (loops)", APPLYING, PROCEDURAL)
    val ilo5 = ILOType(5, "Express unexpected circumstances in the execution flow using language-specific constructs, like ‘Exceptions’ (Java)", APPLYING, PROCEDURAL)
    val ilo6 = ILOType(6, "Select and use appropriate linear data structures", APPLYING, PROCEDURAL)
    // missing ilo7 in dataset
    val ilo8 = ILOType(8, "Select and use appropriate non-linear data structures", APPLYING, PROCEDURAL)
}

@Serializable
data class ExamConfig(val iloTypes : List<ILOType>, val exercises: List<ExamExercise>) {
    init {
        if (!exercises.all { ex -> ex.iloWeights.all { iloWeight -> iloTypes.map { it.id }.contains(iloWeight.iloTypeId) } })
            error("All iloTypeIDs in the exercises should correspond to a valid ILO Type!")
    }
}

@Serializable
data class ExamExercise(val id: Int, val questionDescription: String, val iloWeights: List<ILOWeight>)

@Serializable
data class ILOWeight(val id: Int, val iloTypeId: Int, val totalPoints: Float, val dynamicTestSuites : List<DynamicTestSuite>, val staticTestSuites: List<StaticTestSuite>)

@Serializable
data class ILOType(val id: Int, val description: String, val knowledgeLevel: KnowledgeLevel, val knowledgeKind: KnowledgeKind)

@Serializable
data class DynamicTestSuite(val id: Int,
                            val points: Float,
                            val description: String,
                            val inputTypes : List<String>,     // deserialized with custom serializer (ValNode) due to '\' getting double-escaped sometimes
                            val outputType : String,    // deserialized with custom serializer (ValNode) due to '\' getting double-escaped sometimes
                            val funcName: String,
                            val funcIsStatic : Boolean,
                            val testCases : List<DynamicTestCase>) // TODO: Add input/output variable type?

@Serializable
data class DynamicTestCase(val id : Int,
                           val inputs : List<String>,   // deserialized with custom serializer (ValNode) due to '\' getting double-escaped sometimes
                           val expectedOutput: String?, // deserialized with custom serializer (ValNode) due to '\' getting double-escaped sometimes
                           val exception : String? ) {
    constructor(id: Int, inputs : List<String>, expectedOutput: String) : this(id, inputs, expectedOutput, null)

    init {
        if (expectedOutput == null && exception == null)
            error("Cannot expect a null result and not have an exception!")
    }
}

@Serializable
data class StaticTestSuite(val id:  Int, val points: Float, val description: String, val staticCriteria : List<StaticCriterion>)

@Serializable
data class StaticCriterion(val id : Int, val criterionType : StaticCriterionType, val config : Map<String, List<String>>)

fun main() {
//    val criterion1 = StaticCriterion(1, StaticCriterionType.USES_DATA_STRUCTURE, mapOf("structure" to listOf("list"), "count" to listOf("1")))
//    val criterion2 = StaticCriterion(2, StaticCriterionType.HAS_METHOD_PROPERTIES, mapOf("name" to listOf("countDigits"), "argCount" to listOf("1"), "argType" to listOf("INT"), "modifiers" to listOf("public", "static")))
//    val staticTestSuite = StaticTestSuite(3, 2f, "TestDescriptionStaticStuff", listOf(criterion1, criterion2))
//
//    val dyn1 = DynamicTestCase(1, "INT(0)", "INT(1)")
//    val dyn2 = DynamicTestCase(2, "INT(9)", "INT(1)")
//    val dyn3 = DynamicTestCase(3, "INT(10)", "INT(2)")
//TODO()
////    val dynTestSuite = DynamicTestSuite(1, 3f, "TestTextDynamic stuff", "INT", "INT", "countDigits", listOf(dyn1, dyn2, dyn3))
//
//    val iloWeight1 = ILOWeight(1, /*ModuleILOTypes.ilo1*/1, 5f, listOf(dynTestSuite), listOf(staticTestSuite))
//    val exerciseScheme = ExamExercise(1, "Description for exercise", listOf(iloWeight1))
//
//    val json = Json.encodeToString(exerciseScheme)
//    println(json)
}
