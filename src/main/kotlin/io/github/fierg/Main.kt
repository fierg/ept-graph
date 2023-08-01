package io.github.fierg

import io.github.fierg.algo.Decomposer
import io.github.fierg.algo.Preprocessor
import io.github.fierg.data.DotEnvParser
import io.github.fierg.data.F2FReader
import io.github.fierg.logger.Logger

fun main(args: Array<String>) {
    val options = io.github.fierg.data.ArgParser.readArgs(args)

    if (options.dotenv) {
        DotEnvParser.readDotEnv(options)
    }
    if (options.debug) {
        Logger.setLogLevelToDebug()
    } else if (options.quiet) {
        Logger.setLogLevelToQuiet()
    } else {
        Logger.resetLogLevel()
    }

    val f2fGraph = F2FReader().getF2FNetwork(options.input)
    if (options.deltaWindowPreprocessing > 0)
        Preprocessor.applyDeltaWindow(f2fGraph, options.deltaWindowPreprocessing, options.state)

    Decomposer(options).findComposite(f2fGraph)
}