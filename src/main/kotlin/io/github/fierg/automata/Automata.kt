package io.github.fierg.automata

interface Automata {

    val sigma: List<Int>
    val qStates: List<Int>
    val q1: Int
    val transition: Map<Pair<Int, Int>, Int>
    val fStates: List<Int>

    fun makeTransition(state: Int, vararg symbol: Int): Int
}