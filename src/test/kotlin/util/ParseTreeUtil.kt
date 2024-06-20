package util

import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import org.example.parse.ParsedSubmission
import org.example.parse.SubmissionParserJava
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.fail

// region TRY PARSE
fun tryParseSubmission(submission : String, errorString : String) : ParsedSubmission {
    val res = SubmissionParserJava.parseSubmission(submission)
        ?: fail("$errorString \n\t$submission")
    return res
}

fun tryParseFunctionSubmission(submission: String) : ParsedSubmission {
    return tryParseSubmission(submission, "Could not parse function!")
}
fun tryParseClassSubmission(submission: String) : ParsedSubmission {
    return tryParseSubmission(submission, "Could not parse class!")
}


// region ASSERT CHARACTERISTICS OF AST
fun assertFunctionCharacteristics(funcNode : MethodDeclaration, returnType : String, name : String) {
    assertEquals(returnType, funcNode.type.asString())
    assertEquals(name, funcNode.name.asString())
}

fun assertFieldDeclCharacteristics(fieldDecl : FieldDeclaration, modifiers : List<Modifier.Keyword>, name : String) {
    assertEquals(name, fieldDecl.variables[0].name.asString())
    assertEquals(modifiers, (fieldDecl.modifiers as List<Modifier>).map{it.keyword})
}