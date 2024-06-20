package grading.scheme;

import grading.scheme.enums.KnowledgeKind
import grading.scheme.enums.KnowledgeLevel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.grading.scheme.ExamConfig
import org.example.grading.scheme.ILOType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test;
import util.random.RandUtil.getRandomString
import util.vars.DataVarUtil.getRandomExamExercise
import util.vars.DataVarUtil.getRandomExamConfig
import util.vars.DataVarUtil.getRandomILOWeight

import kotlin.random.Random
import kotlin.test.fail

class GradingSchemeTest {
    private inline fun <reified T> verifyEncodeDecodeWithJson(obj: T) {
        val encodedStr: String = Json.encodeToString(obj)

//        println(encodedStr)
        try {
            val decodedStr: T = Json.decodeFromString(encodedStr)

            assertEquals(obj, decodedStr)
        } catch (e: Exception) {
            fail("Exception occurred while decoding! \n\tObj: $obj \n\tEncoded: $encodedStr \n\tException:$e")
        }
    }

    @Test
    fun testEncodeDecodeILOType() {
        for (knowledgeLevel in KnowledgeLevel.entries) {
            for (knowledgeKind in KnowledgeKind.entries) {
                for (i in 1..10) { // 10 random strings and IDs
                    val iloType = ILOType(Random.nextInt(), getRandomString(0, 50_000), knowledgeLevel, knowledgeKind)

                    verifyEncodeDecodeWithJson(iloType)
                }
            }
        }
    }

    //    @RepeatedTest(5)
//    fun getRandomTestCase() {
//        println(getRandomDynamicTestCasesForType(getRandomType(), getRandomType()))
//    }
    @RepeatedTest(100)
    fun testEncodedDecodeILOWeight() {
        val iloWeight = getRandomILOWeight()
        verifyEncodeDecodeWithJson(iloWeight)
    }

    @RepeatedTest(100)
    fun testEncodeDecodeExamExercise() {
        val examExercise = getRandomExamExercise()
        verifyEncodeDecodeWithJson(examExercise)
    }

    @RepeatedTest(100)
    fun testEncodeDecodeExamConfig() {
        val examConfig = getRandomExamConfig()
        verifyEncodeDecodeWithJson(examConfig)
    }

    @Test
    fun testManualInputFromString() {
        // encode the information on one exam:
        val jsonString = """{
  "iloTypes" : [
    {
      "id" : 1,
      "description" : "Student knows how to use primitive variables",
      "knowledgeLevel" : "APPLYING",
      "knowledgeKind" : "PROCEDURAL"
    }
  ],

  "exercises": [{
    "id" : 1,
    "questionDescription" : "TestDescription for exam exercise :)",
    "iloWeights" : [
      {
        "id" : 1,
        "iloTypeId" : 1,
        "totalPoints" : 5.0,
        "dynamicTestSuites" : [
          {
            "id" : 1,
            "points" : 3.0,
            "description" : "Test whether days of the week are correctly implemented",
            "inputTypes" : ["NUM"],
            "outputType" : "STR",
            "funcName" : "WeekDay",
            "funcIsStatic" : true,
            "testCases" : [
              { "id" : 1, "input" : "INT(0)", "expectedOutput" : "STR(Sunday)", "exception": null },
              { "id" : 2, "input" : "INT(1)", "expectedOutput" : "STR(Monday)", "exception": null },
              { "id" : 3, "input" : "INT(7)", "expectedOutput" : "STR(Sunday)", "exception": null },
              { "id" : 4, "input" : "INT(8)", "expectedOutput" : null, "exception" : "IllegalArgumentException" }
            ]
          }
        ],
        "staticTestSuites" : [
          {
            "id" : 1,
            "points" : 2.0,
            "description" : "Student utilises IF or SWITCH",
            "staticCriteria" : [
              {
                "id" : 1,
                "criterionType" : "USES_DATA_STRUCTURE",
                "config" : {
                  "structures" : ["IF", "SWITCH"]
                }
              }
            ]
          },
          {
            "id" : 1,
            "points" : 2.0,
            "description" : "Student does not use arrays or maps",
            "staticCriteria" : [
              {
                "id" : 1,
                "criterionType" : "USES_DATA_STRUCTURE",
                "config" : {
                  "deduce_points" : ["true"],
                  "structures" : ["LIST", "MAP"]
                }
              }
            ]
          }
        ]
      }
    ]
  }]
}"""
        val examConfig: ExamConfig = Json.decodeFromString(jsonString)
        println(examConfig)
    }
}
