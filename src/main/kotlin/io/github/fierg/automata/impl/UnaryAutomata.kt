package io.github.fierg.automata

class UnaryAutomata(private val input: BooleanArray) {

    init {
        input.forEachIndexed { index, b ->
            if (b) fStates.add(index)
        }
    }

    val sigma = listOf(0)
    val qStates = (0..input.size).toList()
    val q1 = 0
    val transition: Map<Pair<Int, Int>, Int> = mutableMapOf()
    val fStates = mutableListOf<Int>()


    private fun performTransition(state: Int, vararg symbol: Int): Int {
        return if (symbol.size == 1) transition[Pair(state, symbol.first())]!!
        else performTransition(transition[Pair(state, symbol.first())]!!, *symbol.copyOfRange(1,symbol.size))
    }
}