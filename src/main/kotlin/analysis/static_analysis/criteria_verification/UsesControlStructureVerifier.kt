package analysis.static_analysis.criteria_verification

import analysis.static_analysis.criteria_verification.ControlStructureCounter.Companion.ALLOWED_KEYWORDS
import com.github.javaparser.ast.stmt.*
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import grading.scheme.enums.StaticCriterionType
import kotlinx.collections.immutable.persistentListOf
import org.example.analysis.static_analysis.criteria_verification.SelectionMethod
import org.example.grading.scheme.StaticCriterion
import org.example.parse.ParsedSubmission

class ControlStructureCounter(val structsToCheck : List<String>) :  VoidVisitorAdapter<MutableMap<String, Int>>() {
    private fun incrementMapKey(map : MutableMap<String, Int>?, key : String) {
        map?.put(key, if (map[key] != null) map[key]!! else 1)
    }
    override fun visit(n: DoStmt?, arg: MutableMap<String, Int>?) {
        super.visit(n, arg)
        incrementMapKey(arg, "do")
    }

    override fun visit(n: ForEachStmt?, arg: MutableMap<String, Int>?) {
        super.visit(n, arg)
        incrementMapKey(arg, "for")
    }

    override fun visit(n: ForStmt?, arg: MutableMap<String, Int>?) {
        super.visit(n, arg)
        incrementMapKey(arg, "for")
    }

    override fun visit(n: SwitchStmt?, arg: MutableMap<String, Int>?) {
        super.visit(n, arg)
        incrementMapKey(arg, "switch")
    }

    override fun visit(n: WhileStmt?, arg: MutableMap<String, Int>?) {
        super.visit(n, arg)
        incrementMapKey(arg, "while")
    }
    companion object {
        val ALLOWED_KEYWORDS = persistentListOf("do", "for", "switch", "while")
    }
}

/**
 * Config layout:
 *  {
 *      "structures" : [<ctrlStruct>, ...],
 *      "selectionMethod" : ["ALL"]/["ANY"]
 *  }
 *
 *  where ctrlStruct is "for"/"while"/"switch"/"do"
 *  selection method denotes whether to strictly check if all the structures occur within the solution, or whether any structure in the "structures" list is fine.
 */
object UsesControlStructureVerifier : StaticCriterionVerifier {
    // Decides whether the control structures must ALL be present (ALL), or whether ANY of the control structure can be present (ANY). Default is ALL.

    private fun checkControlStructure(criterion: StaticCriterion, submission: ParsedSubmission): Pair<Boolean, Any?> {
        val ctrlStructs = criterion.config["structures"] ?: error("Please input \"structures\" as key when verifying control structures!")
        if (!ctrlStructs.all { ALLOWED_KEYWORDS.contains(it) }) {
            error("Please enter valid structure values! (allowed: $ALLOWED_KEYWORDS, actual: $ctrlStructs)")
        }
        val selectionMethod : String = criterion.config["selectionMethod"]?.get(0) ?: SelectionMethod.default().toString()
        if (!SelectionMethod.entries.map { it.toString() }.contains(selectionMethod))
            error("Please specify a valid checking method (allowed: ${SelectionMethod.entries.toList()})")
        val checkingMethod : SelectionMethod = SelectionMethod.valueOf(selectionMethod)


        val counts : MutableMap<String, Int> = mutableMapOf()
        val counter = ControlStructureCounter(ctrlStructs)

        if (submission.methodDeclaration != null)
            counter.visit(submission.methodDeclaration, counts)
        else if (submission.compilationUnit != null)
            counter.visit(submission.compilationUnit, counts)
        else
           error("Invalid ParsedSubmission, methodDecl and compilationUnit were both null!")

        val criterionSatisfied : Boolean = when(checkingMethod) {
            SelectionMethod.ALL -> { ctrlStructs.all { counts.containsKey(it) } }
            SelectionMethod.ANY -> { ctrlStructs.any { counts.containsKey(it) } }
        }

        return Pair(criterionSatisfied, counts)
    }

    override fun verifyCriterion(criterion: StaticCriterion, submission: ParsedSubmission): Pair<Boolean, Any?> {
        return when (criterion.criterionType) {
            StaticCriterionType.USES_CONTROL_STRUCTURE -> {
                checkControlStructure(criterion, submission)
            }

            else -> error("UsesDataStructureVerifier cannot verify a criterion other than USES_DATA_STRUCTURE!")
        }
    }
}