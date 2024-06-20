package analysis.static_analysis

import analysis.static_analysis.criteria_verification.StaticCriterionVerifier
import com.github.javaparser.ast.Node
import org.example.grading.scheme.*
import org.example.parse.ParsedSubmission
import kotlin.reflect.KClass

object StaticAnalyser {
    fun analyseStaticTestSuite(
        testSuite: StaticTestSuite,
        submission: ParsedSubmission
    ): StaticTestSuiteGrade {
        val criterionGrades: MutableList<StaticCriterionGrade> = mutableListOf()

        for (criterion in testSuite.staticCriteria) {
            // get proper verifier for criterion:
            val verifier: StaticCriterionVerifier = criterion.criterionType.getCriterionChecker()

            val result: Pair<Boolean, Any?> = verifier.verifyCriterion(criterion, submission)
            criterionGrades += StaticCriterionGrade(
                criterion.id,
                result.first,
                result.second?.toString()
            )
        }

        return StaticTestSuiteGrade(
            testSuite.id,
            if (criterionGrades.all { it.passed }) testSuite.points else 0f,
            criterionGrades
        )
    }
}
