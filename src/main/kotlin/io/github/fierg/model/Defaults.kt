package io.github.fierg.model

import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.scale.scaleFillManual
import org.jetbrains.letsPlot.themes.elementBlank
import org.jetbrains.letsPlot.themes.theme

class Defaults {
    companion object {
        const val DEFAULT_WIDTH = 600
        const val DEFAULT_HEIGHT = 375
        val blankTheme = theme(axisLine = elementBlank(), axis = elementBlank(), panelGrid = elementBlank())
        val defaultPieCharConfig = scaleFillManual(values = listOf("#61BAFF", "#03FF07", "#ADF527","#d4FF00", "#FF9500", "#FF0000")) + blankTheme
        val defaultStyle = Style.PERCENT

    }
}