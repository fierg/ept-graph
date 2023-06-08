package io.github.fierg.graph

import io.github.fierg.model.SelfAwareEdge
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.builder.GraphBuilder

class EPTGraph(val nodes: List<Int>, val edges: Collection<SelfAwareEdge>, val steps: MutableMap<SelfAwareEdge, BooleanArray>, val nodeLabels: List<String>?) {

    fun getGraphAtStep(step: Int): DefaultDirectedGraph<Int, SelfAwareEdge>? {
        val graph = GraphBuilder<Int, SelfAwareEdge, DefaultDirectedGraph<Int, SelfAwareEdge>>(DefaultDirectedGraph(SelfAwareEdge::class.java))
        graph.addVertices(*nodes.toTypedArray())

        edges.forEach { edge ->
            if (steps[edge]!![step % steps[edge]!!.size]) {
                graph.addEdge(edge.source, edge.target, edge)
            }
        }
        return graph.build()
    }
}