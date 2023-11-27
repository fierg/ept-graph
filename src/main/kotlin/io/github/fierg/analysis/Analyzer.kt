package io.github.fierg.analysis

import io.github.fierg.logger.Logger
import io.github.fierg.model.result.Cover
import org.jetbrains.letsPlot.intern.Plot

class Analyzer {

    companion object {
        fun createCoverByFactorPlotSum(covers: List<Cover>, normalized: Boolean = false, byFactorNr: Boolean = false): Plot {
            val xS = "Rel Factor Size"
            val yS = "Sum of covered values"
            Logger.info("Collecting values to plot...")
            val resultMap = mutableMapOf<Double, Int>()
            var totalValuesToCover = 0
            covers.forEach { cover ->
                cover.factors.forEach { factor ->
                    val size = factor.getRelativeSize(cover)
                    if (resultMap[size] == null) resultMap[size] = 0
                }
            }

            covers.forEach { cover ->
                totalValuesToCover += cover.totalValues
                cover.factors.forEach { factor ->
                    val size = factor.getRelativeSize(cover)
                    val value = factor.getCoveredValuesUntilThisFactor(cover)
                    resultMap.keys.filter { it >= size }.forEach { factorSize ->
                        resultMap[factorSize] = resultMap[factorSize]!! + value
                    }
                }
            }

            return Visualizer.generatePointPlot(resultMap, totalValuesToCover, xS, yS, normalized, byFactorNr)
        }

        fun createCoverByFactorPlotNormalized(covers: List<Cover>, useAverage: Boolean = false, createBoxPlot: Boolean = false, showOutliers: Boolean = false, minDistance: Double = 0.0, useFactorNrInsteadOfSize: Boolean = false): Plot {
            val xS = "Rel Factor Size"
            val yS = "Rel amount of values covered"
            Logger.info("Collecting values to plot...")
            val resultMap = mutableMapOf<Double, MutableList<Double>>()
            covers.forEach { cover ->
                cover.factors.forEach { factor ->
                    val size = factor.getRelativeSize(cover)
                    if (resultMap[size].isNullOrEmpty()) {
                        resultMap[size] = mutableListOf(factor.getRelativeCoveredValues(cover))
                    } else {
                        resultMap[size]!!.add(factor.getRelativeCoveredValues(cover))
                    }
                }
            }

            return if (createBoxPlot)
                Visualizer.generateBoxPlotForNormalized(resultMap, xS, yS, minDistance, showOutliers, useFactorNrInsteadOfSize)
            else
                Visualizer.generatePointPlotForNormalized(resultMap, xS, yS, useAverage)
        }

        fun createCoverByFactorPlot(covers: List<Cover>): Plot {
            val xS = "Rel Factor Size"
            val yS = "Sum of covered values"
            Logger.info("Collecting values to plot...")
            val resultMap = mutableMapOf<Double, Pair<Int, Int>>()
            covers.forEach { cover ->
                cover.factors.forEach { factor ->
                    val size = factor.getRelativeSize(cover)
                    if (resultMap[size] == null) {
                        resultMap[size] = Pair(cover.totalValues - factor.outliers.size, cover.totalValues)
                    } else {
                        resultMap[size] = Pair(resultMap[size]!!.first + cover.totalValues - factor.outliers.size, resultMap[size]!!.second + cover.totalValues)

                    }
                }
            }

            return Visualizer.generatePointPlot1(resultMap, xS, yS)
        }
    }
}