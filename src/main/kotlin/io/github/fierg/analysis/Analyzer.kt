package io.github.fierg.analysis

import io.github.fierg.logger.Logger
import io.github.fierg.model.result.Cover
import org.jetbrains.letsPlot.intern.Plot

class Analyzer {

    companion object {
        fun createCoverByFactorPlotSum(covers: List<Cover>, normalized: Boolean = false, byFactorNr: Boolean = false, fitCurve: Boolean = false, xS: String, yS: String): Plot {
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

            return Visualizer.generatePointPlot(resultMap, totalValuesToCover, xS, yS, normalized, byFactorNr, fitCurve)
        }

        fun createCoverByFactorPlotNormalized(covers: List<Cover>, useAverage: Boolean = false, createBoxPlot: Boolean = false, showOutliers: Boolean = false, minDistance: Double = 0.0, useFactorNrInsteadOfSize: Boolean = false, xS: String, yS: String): Plot {
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

    }
}