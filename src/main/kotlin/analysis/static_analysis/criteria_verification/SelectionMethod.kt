package org.example.analysis.static_analysis.criteria_verification

enum class SelectionMethod {
    ALL,
    ANY;

    companion object {
        fun default() = ALL
    }
}