package org.example.util.csv.parser

data class Submission(
    val resultID : Int, // unique id for result
    val candidateID : Int, // id for candidate (obfuscated alternative to student ID)
    val questionNumber : Int,
    val questionID : Int, // id (unique per question number?)
    val questionCode : String, // unique identifier per question
    val interactionNumber : Int,
    val variableName : String,
    val variableValue : String // the actual submission code
) {
    override fun toString(): String {
        return "Submission(id=$resultID, candidateID=$candidateID, q=$questionNumber, code=\n```java\n$variableValue```)"
    }
}