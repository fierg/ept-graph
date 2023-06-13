package io.github.fierg

import io.github.fierg.algo.Decomposition
import io.github.fierg.analysis.PeriodAnalyzer
import io.github.fierg.data.FileReader
import io.github.fierg.model.CompositionMode
import org.junit.Test

class TestAnalyzer {

    @Test
    fun testAnalyzer(){
        val f2fGraph = FileReader().getF2FNetwork(0)
        val edge = f2fGraph.edges.elementAt(6)
        val decomposition = Decomposition(mode = CompositionMode.SIMPLE, clean = true, coroutines = true, state = false, deltaWindowAlgo = 0)
        val periods = decomposition.findCover(f2fGraph.steps[edge]!!)
        decomposition.analyze(f2fGraph, edge, periods)

        val plot = PeriodAnalyzer.analyzePeriods(periods)
        PeriodAnalyzer.saveToFile("test-plot.html", plot)
    }
}