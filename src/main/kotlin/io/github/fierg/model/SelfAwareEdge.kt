package io.github.fierg.model

import org.jgrapht.graph.DefaultEdge

class SelfAwareEdge(val source: Int, val target: Int) : DefaultEdge() {

    override fun toString(): String {
        return "($source,$target)"
    }
}