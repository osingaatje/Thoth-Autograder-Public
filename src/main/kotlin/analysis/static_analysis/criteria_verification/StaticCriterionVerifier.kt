package analysis.static_analysis.criteria_verification

import com.github.javaparser.ast.Node
import org.example.grading.scheme.StaticCriterion
import org.example.parse.ParsedSubmission

interface StaticCriterionVerifier {
    fun verifyCriterion(criterion: StaticCriterion, submission: ParsedSubmission): Pair<Boolean, Any?>
}