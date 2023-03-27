package io.github.fierg

import io.github.fierg.data.FileReader

fun main(){
    val simpleGraph = FileReader().readSimpleGraph("data/stanford/wiki-talk-temporal.txt")
    val f2fGraph = FileReader().readF2FNetwork(0)


    simpleGraph
}