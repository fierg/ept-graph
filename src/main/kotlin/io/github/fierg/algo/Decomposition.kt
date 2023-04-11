package io.github.fierg.algo

import io.github.fierg.automata.impl.UnaryAutomata
import io.github.fierg.extensions.factorsSequence

class Decomposition {

    fun findComposite(a: UnaryAutomata): Boolean {
        val factors = a.qStates.size.factorsSequence().iterator()
        a.qStates.forEach { p ->
            factors.forEach { factor ->

            }
        }
        return false
    }
}