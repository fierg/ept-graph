package io.github.fierg.model

data class EvaluationResult(
    val factors: Map<Int, Int>,
    val covers: Map<Int, Int>,
    val totalValues: Int,
    val totalPeriods: Int
)
