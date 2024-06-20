package grading.scheme.enums

import analysis.static_analysis.criteria_verification.StaticCriterionVerifier
import analysis.static_analysis.criteria_verification.UsesControlStructureVerifier
import org.example.analysis.static_analysis.criteria_verification.UsesFunctionVerifier
import kotlin.reflect.KClass

enum class StaticCriterionType {
    USES_CONTROL_STRUCTURE,
    USES_FUNCTION
    ;

    fun getCriterionChecker() : StaticCriterionVerifier {
        return when(this) {
            USES_CONTROL_STRUCTURE -> UsesControlStructureVerifier
            USES_FUNCTION -> UsesFunctionVerifier
        }
    }
}
