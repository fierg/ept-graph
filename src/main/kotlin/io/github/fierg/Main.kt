package io.github.fierg

import io.github.fierg.algo.Decomposer
import io.github.fierg.algo.Preprocessor
import io.github.fierg.data.DotEnvParser
import io.github.fierg.data.FileReader
import io.github.fierg.logger.Logger
import kotlin.system.exitProcess


fun main(args: Array<String>) {

    val options = io.github.fierg.data.ArgParser.readArgs(args)
    if (options.dotenv) {
        DotEnvParser.readDotEnv(options)
    }

    if (options.mode == null) {
        Logger.error("Mode not provided! Exiting.")
        exitProcess(1)
    }

    if (options.debug) {
        Logger.setLogLevelToDebug()
    }
    if (options.quiet) {
        Logger.setLogLevelToQuiet()
    }

    val f2fGraph = FileReader().getF2FNetwork(options.input)
    if (options.deltaWindowPreprocessing > 0)
        Preprocessor.applyDeltaWindow(f2fGraph, options.deltaWindowPreprocessing, options.state)

    Decomposer(options).findComposite(f2fGraph)
}