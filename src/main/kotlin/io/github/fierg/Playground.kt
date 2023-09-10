package io.github.fierg

import io.github.fierg.algo.Decomposer
import io.github.fierg.data.F2FReader
import io.github.fierg.extensions.factorsSequence

fun main(){
    val array = arrayOf(true,false,false,true,true,false).toBooleanArray()
    val number = 24
    println("Factors of $number are ${number.factorsSequence().toList()}")

    val periods = Decomposer().findCover(array)

    Decomposer().findCover(BooleanArray(16))

    val f2fGraph = F2FReader().getF2FNetwork(4)
    val decomposition = Decomposer(state = false, deltaWindowAlgo = 0)
    val decompositionResult = decomposition.findComposite(f2fGraph)
}