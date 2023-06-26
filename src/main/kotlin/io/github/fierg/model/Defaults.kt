package io.github.fierg.model

import org.jetbrains.letsPlot.themes.elementBlank
import org.jetbrains.letsPlot.themes.theme

class Defaults {
    companion object {
        const val DEFAULT_WIDTH = 600
        const val DEFAULT_HEIGHT = 375
        val blankTheme = theme(axisLine = elementBlank(), axis = elementBlank(), panelGrid = elementBlank())
    }
}