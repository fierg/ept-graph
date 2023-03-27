package io.github.fierg

import io.github.fierg.data.FileReader
import io.github.fierg.periodic.Periodic

fun main(){
    //val simpleGraph = FileReader().readSimpleGraph("data/stanford/wiki-talk-temporal.txt")
    val f2fGraph = FileReader().getF2FNetwork(0)
    f2fGraph.getGraphAtStep(1)

    val period = Periodic().findShortestPeriod(arrayOf(true,false,false,true,false,false).toBooleanArray())
    println("period is $period")
}