package org.example.analysis.dynamic_analysis

data class DynamicTestCaseResult(val expectedOutput: Any?, val actualOutput: Any?, val passed: Boolean, val inputs: Array<out Any?>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DynamicTestCaseResult

        if (expectedOutput != other.expectedOutput) return false
        if (actualOutput != other.actualOutput) return false
        if (passed != other.passed) return false
        if (!inputs.contentEquals(other.inputs)) return false
        return true
    }

    // auto-generated
    override fun hashCode(): Int {
        var result = expectedOutput.hashCode()
        result = 31 * result + actualOutput.hashCode()
        result = 31 * result + passed.hashCode()
        result = 31 * result + inputs.contentHashCode()
        return result
    }
}