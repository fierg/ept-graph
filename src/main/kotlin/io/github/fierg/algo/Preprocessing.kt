package io.github.fierg.algo

import io.github.fierg.graph.EPTGraph
import io.github.fierg.logger.Logger


class Preprocessing {
    companion object {
        fun applyDeltaWindow(input: BooleanArray, width: Int): BooleanArray {
            val length = input.size
            val output = BooleanArray(length)

            // Iterate over the input array
            for (i in 0 until length) {
                val start = 0.coerceAtLeast(i - width)
                val end = (length - 1).coerceAtMost(i + width)

                // Apply the delta window function
                for (j in start..end) {
                    if (input[j]) {
                        output[i] = true
                        break
                    }
                }
            }
            return output
        }

        fun applyDeltaWindow(input: EPTGraph, width: Int) {
            Logger.info("Applying Delta window as preprocessing with width of $width.")
            input.steps.forEach { (t, u) ->
                input.steps[t] = applyDeltaWindow(u, width)
            }
        }
    }
}