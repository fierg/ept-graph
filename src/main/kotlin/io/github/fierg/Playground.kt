package io.github.fierg

import io.github.fierg.algo.Decomposition
import io.github.fierg.extensions.factorsSequence
import io.github.fierg.periodic.Periodic

fun main(){
    val array = arrayOf(true,false,false,true,true,false).toBooleanArray()
    val period = Periodic().findShortestPeriod(array)
    println("period is $period")

    val number = 24
    println("Factors of $number are ${number.factorsSequence().toList()}")

    val periods = Decomposition().findCover(array)
    periods

    Decomposition().findCover(BooleanArray(16))

}