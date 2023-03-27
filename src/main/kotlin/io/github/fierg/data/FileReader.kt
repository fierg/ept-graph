package io.github.fierg.data

import io.github.fierg.graph.EPTGraph
import io.github.fierg.logger.Logger
import io.github.fierg.model.LabeledEdge
import io.github.fierg.model.SelfAwareEdge
import io.github.fierg.periodic.Periodic
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.builder.GraphBuilder
import java.io.File
import java.nio.charset.Charset

class FileReader {
    private val logger = Logger

    fun readSimpleGraph(file: String): DefaultDirectedGraph<Int, LabeledEdge> {
        logger.info("Reading graph from file $file ...")
        val regex = Regex("(\\d+) (\\d+) (\\d+)")
        val graph = GraphBuilder<Int, LabeledEdge, DefaultDirectedGraph<Int, LabeledEdge>>(DefaultDirectedGraph(LabeledEdge::class.java))
        val nodes = mutableSetOf<Int>()
        var edges = 0
        File(file).readLines(Charset.defaultCharset()).forEach { line ->
            if (regex.matches(line)) {
                val groups = regex.find(line)!!.groupValues
                val source = groups[1].toInt()
                val target = groups[2].toInt()
                graph.addVertex(source)
                graph.addVertex(target)
                nodes.add(source)
                nodes.add(target)
                edges++
            } else {
                logger.error("Unexpected line: $line")
            }

        }
        logger.info("Done. Read graph with ${nodes.size} nodes and $edges edges.")
        return graph.build()
    }

    fun getF2FNetwork(id: Int): EPTGraph {
        if (id > 61) throw IllegalArgumentException("ID needs to be in range [0,61]")
        logger.info("Reading network$id from file data/f2f/network/network$id.csv")
        val networkList = File("data/f2f/network_list.csv")
        val people = networkList.readLines()[id + 1].split(",")[1].toInt()
        val edges = mutableListOf<SelfAwareEdge>()
        val steps = mutableMapOf<SelfAwareEdge, MutableList<Boolean>>()
        val labels = readF2FFile(id,people, steps, edges)
        val nrOfSteps = steps[edges.last()]!!.size
        val shortenedSteps = steps.map {
            val array = it.value.toBooleanArray()
            val period = Periodic().findShortestPeriod(array)
            if (period == nrOfSteps) {
                it.key to array
            } else {
                it.key to array.copyOfRange(0, period)
            }
        }.toMap()
        return EPTGraph(nodes = (0..people).toList(), edges, shortenedSteps, labels)
    }

    private fun readF2FFile(id: Int, people: Int, steps: MutableMap<SelfAwareEdge, MutableList<Boolean>>, edges: MutableList<SelfAwareEdge>): List<String> {
        logger.info("Expecting $people people in network (${people + 1} nodes) and ${people * (people + 1)} edges.")

        for (i in 0 until (people * (people + 1))) {
            val edge = SelfAwareEdge((i / (people + 1)) + 1, i % (people + 1))
            edges.add(edge)
            steps[edge] = mutableListOf()
        }

        var labels = emptyList<String>()
        File("data/f2f/network/network$id.csv").readLines(Charset.defaultCharset()).forEachIndexed { index, line ->
            if (index == 0) {
                labels = line.split(",")
                labels = labels.subList(1, labels.size)
            } else if (index > 0) {
                val values = line.split(",")
                values.forEachIndexed { token, s ->
                    if (token > 0) {
                        when (s) {
                            "0" -> steps[edges[token - 1]]!!.add(false)
                            "1" -> steps[edges[token - 1]]!!.add(true)
                            else -> logger.error("unexpected token in line $index at token $token:\n$line")
                        }
                    }
                }
            }
        }
        logger.info("EPT Graph has ${steps[edges.first()]!!.size - 1} time steps.")
        return labels
    }
}