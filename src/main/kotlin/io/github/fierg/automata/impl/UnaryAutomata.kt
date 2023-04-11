package io.github.fierg.automata.impl

import io.github.fierg.automata.Automata

class UnaryAutomata(input: BooleanArray) : Automata {

    override val sigma = listOf(0)
    override val qStates = (0..input.size).toList()
    override val q1 = 0
    override val transition: Map<Pair<Int, Int>, Int> = mutableMapOf()
    override val fStates = mutableListOf<Int>()

    init {
        //Init states
        input.forEachIndexed { index, b ->
            if (b) fStates.add(index)
        }
        //Init transitions
        //TODO: is this really required ?
    }


    override fun makeTransition(state: Int, vararg symbol: Int): Int {
        return if (symbol.size == 1) transition[Pair(state, symbol.first())]!!
        else makeTransition(transition[Pair(state, symbol.first())]!!, *symbol.copyOfRange(1, symbol.size))
    }
}