package io.github.fierg.model

class SelfAwareEdge(val source: Int, val target: Int) {

    override fun toString(): String {
        return "($source,$target)"
    }
}