package io.github.fierg

import io.github.fierg.algo.Decomposition
import io.github.fierg.data.FileReader
import io.github.fierg.logger.Logger
import io.github.fierg.model.CompositionMode
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlin.system.exitProcess
import io.github.cdimascio.dotenv.dotenv
import io.github.fierg.algo.Preprocessing
import io.github.fierg.model.ENV


fun main(args: Array<String>) {

    val parser = ArgParser("EPT Graph Reader")

    val dotenv by parser.option(ArgType.Boolean, shortName = "env", description = "Use config from .env file (Recommended usage due to amount of args)").default(false)
    val input by parser.argument(ArgType.Int, description = "input (Network id in range (0..61)")
    var state by parser.option(ArgType.Boolean, description = "Invert state to substitute in decomposition (if set, decomposition will replace 0s instead of 1s)", shortName = "s").default(false)
    var coroutines by parser.option(ArgType.Boolean, description = "Use Coroutines for period computation. (Use with check)", shortName = "co").default(false)
    var clean by parser.option(ArgType.Boolean, description = "Clean up periods of multiples", shortName = "cl").default(false)
    var mode by parser.option(ArgType.Choice<CompositionMode>(), shortName = "m", description = "Mode of composing the periods [ALL,SIMPLE,GREEDY]")
    var debug by parser.option(ArgType.Boolean, shortName = "d", description = "Turn on debug mode").default(false)
    var quiet by parser.option(ArgType.Boolean, shortName = "q", description = "Turn on quiet mode").default(false)
    var deltaWindowPreprocessing by parser.option(ArgType.Int, description = "Delta window in preprocessing of the label").default(0)
    var deltaWindowAlgo by parser.option(ArgType.Int, description = "Delta window during decomposing").default(0)


    parser.parse(args = args)
    if (dotenv) {
        val env = dotenv {
            directory = "./"
            ignoreIfMalformed = true
            ignoreIfMissing = true
            systemProperties = true
        }
        Logger.info("Parsing arg from .env file:\n${env.entries().filter { envEntry -> ENV.values().map { envVar -> envVar.toString() }.contains(envEntry.key) }.map { "${it.key}:${it.value}\n" }}")
        state = env["STATE"] == "true"
        coroutines = env["COROUTINES"] == "true"
        clean = env["CLEAN"] == "true"
        mode = when (env["MODE"]) {
            "ALL" -> CompositionMode.ALL
            "SIMPLE" -> CompositionMode.SIMPLE
            "GREEDY" -> CompositionMode.GREEDY
            else -> {
                Logger.error("Composition Mode missing! Running with ALL")
                CompositionMode.ALL
            }
        }
        deltaWindowPreprocessing = env["DELTA_WINDOW_PREPROCESSING"].toInt()
        deltaWindowAlgo = env["DELTA_WINDOW_ALGO"].toInt()
        debug = env["DEBUG"] == "true"
        quiet = env["QUIET"] == "true"
    }


    state = !state

    if (debug) {
        Logger.setLogLevelToDebug()
    }

    if (quiet) {
        Logger.setLogLevelToQuiet()
    }

    if (mode == null) {
        Logger.error("Mode not provided! Exiting.")
        exitProcess(1)
    }

    val f2fGraph = FileReader().getF2FNetwork(input)
    if (deltaWindowPreprocessing > 0)
        Preprocessing.applyDeltaWindow(f2fGraph, deltaWindowPreprocessing, state)

    Decomposition(state, coroutines, clean, mode!!, deltaWindowAlgo).findComposite(f2fGraph)

}