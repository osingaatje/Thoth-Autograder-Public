package org.example.analysis.static_analysis.criteria_verification

import analysis.static_analysis.criteria_verification.StaticCriterionVerifier
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import org.example.analysis.static_analysis.criteria_verification.UsesFunctionVerifier.INPUT_VAL
import org.example.analysis.static_analysis.criteria_verification.UsesFunctionVerifier.UNKNOWN_KEY
import org.example.grading.scheme.StaticCriterion
import org.example.parse.ParsedSubmission

class FuncNameVisitor : VoidVisitorAdapter<MutableMap<String, MutableList<String>>>() {
    private var methodArgNames: List<String> = listOf()
    override fun visit(n: MethodDeclaration?, arg: MutableMap<String, MutableList<String>>?) {
        methodArgNames = n?.parameters?.map { it.nameAsString } ?: listOf()
        super.visit(n, arg)
    }

    override fun visit(n: MethodCallExpr?, arg: MutableMap<String, MutableList<String>>?) {
        super.visit(n, arg)

        if (n != null) {
            var scopeName = UNKNOWN_KEY // default is "?", unless an explicit variable is provided
            n.scope.ifPresent { scopeName = it.toString() }

            // replace any variable that is an input variable with the predefined key <ARG>
            if (methodArgNames.contains(scopeName)) {
                scopeName = INPUT_VAL
            }

            // put the key and value in the map :)
            if (arg != null && arg.containsKey(scopeName)) {
                arg[scopeName]!!.add(n.name.toString())
            } else if (arg != null) {
                arg[scopeName] = mutableListOf(n.name.toString())
            }
        }
    }
}

/**
 * Config layout:
 *  {
 *      "methods" : [<methodName>, ...],
 *      "selectionMethod" : ["ALL"]/["ANY"]
 *  }
 *
 *  where selectionMethod denotes whether to only give points if all methods are used, or if one or more method out of the list is used.
 */
object UsesFunctionVerifier : StaticCriterionVerifier {
    const val INPUT_VAL = "<ARG>"
    const val UNKNOWN_KEY = "?"

    private fun convertToScopedFunctionCriteria(funcNames: List<String>): Map<String, List<String>> {
        // if the function contains a dot '.', then we take the scope as the string before the dot.
        val res: MutableMap<String, MutableList<String>> = mutableMapOf()

        funcNames.forEach {
            if (it.contains('.')) {
                val scope = it.substringBeforeLast('.')
                val funcName = it.substringAfterLast('.')
                if (res.containsKey(scope))
                    res[scope]!! += funcName
                else
                    res[scope] = mutableListOf(funcName)

            } else {
                if (res.containsKey(UNKNOWN_KEY))
                    res[UNKNOWN_KEY]!! += it
                else
                    res[UNKNOWN_KEY] = mutableListOf(it)
            }
        }
        return res
    }

    override fun verifyCriterion(criterion: StaticCriterion, submission: ParsedSubmission): Pair<Boolean, Any?> {
        if (!criterion.config.containsKey("funcNames"))
            error("Please enter configuration for which function names to check!")
        if (!criterion.config.containsKey("selectionMethod") || criterion.config["selectionMethod"]!!.size != 1)
            error("Please enter exactly one selection method you want to use for funcNames (available values: ${SelectionMethod.entries.toList()})")

        val funcNames: Map<String, List<String>> = convertToScopedFunctionCriteria(criterion.config["funcNames"]!!)
        val selectionMethod: SelectionMethod = SelectionMethod.valueOf(criterion.config["selectionMethod"]!![0])

        val submissionScopesFuncNames: MutableMap<String, MutableList<String>> = mutableMapOf()
        val visitor = FuncNameVisitor()

        if (submission.compilationUnit != null)
            visitor.visit(submission.compilationUnit, submissionScopesFuncNames)
        else if (submission.methodDeclaration != null)
            visitor.visit(submission.methodDeclaration, submissionScopesFuncNames)
        else
            error("Invalid submission! Contained no compilation unit or method declaration (UsesFuncVerifier).")

        // check the unknown keys (absolute methods), and the variable-specific methods
        val criterionSatisfied: Boolean = when (selectionMethod) {
            SelectionMethod.ALL -> {
                funcNames[UNKNOWN_KEY]?.all {
                    submissionScopesFuncNames.values.flatten().contains(it)
                } ?: true // the absolute function calls are in UNKNOWN key for the config. ('?')
                        && funcNames.filter { it.key != UNKNOWN_KEY }.all { fNameEntry ->
                    fNameEntry.value.all { fNameFunc ->
                        submissionScopesFuncNames[fNameEntry.key]?.contains(fNameFunc) ?: false
                    }
                }
            }

            SelectionMethod.ANY -> {
                funcNames[UNKNOWN_KEY]?.any {
                    submissionScopesFuncNames.values.flatten().contains(it)
                } ?: false // the absolute function calls are in UNKNOWN key for the config. ('?')
                        || funcNames.filter { it.key != UNKNOWN_KEY }.any { fNameEntry ->
                    fNameEntry.value.any { fNameFunc ->
                        submissionScopesFuncNames[fNameEntry.key]?.contains(fNameFunc) ?: false
                    }
                }
            }//funcNames.any { submissionFuncNames.contains(it) } } // at least one func name out of all predetermined names must be used
        }
        return Pair(criterionSatisfied, submissionScopesFuncNames)
    }
}