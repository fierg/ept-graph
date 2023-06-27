package io.github.fierg.data

import io.github.cdimascio.dotenv.dotenv
import io.github.fierg.logger.Logger
import io.github.fierg.model.CompositionMode
import io.github.fierg.model.ENV
import io.github.fierg.model.Options

class DotEnvParser {
    companion object{
        fun readDotEnv(options:Options = Options.emptyOptions()): Options {
            val env = dotenv {
                directory = "./"
                ignoreIfMalformed = true
                ignoreIfMissing = true
                systemProperties = true
            }
            Logger.info("Parsing arg from .env file:\n${env.entries().filter { envEntry -> ENV.values().map { envVar -> envVar.toString() }.contains(envEntry.key) }.map { "${it.key}:${it.value}\n" }}")
            options.state = env["STATE"] == "true"
            options.coroutines = env["COROUTINES"] == "true"
            options.clean = env["CLEAN"] == "true"
            options.skipSingleStepEdges = env["SKIP_SINGLE_STEP_EDGES"] == "true"

            options.mode = when (env["MODE"]) {
                "ALL" -> CompositionMode.ALL
                "SIMPLE" -> CompositionMode.SIMPLE
                "GREEDY" -> CompositionMode.GREEDY
                else -> {
                    Logger.error("Composition Mode missing! Running with ALL")
                    CompositionMode.ALL
                }
            }
            options.deltaWindowPreprocessing = env["DELTA_WINDOW_PREPROCESSING"].toInt()
            options.deltaWindowAlgo = env["DELTA_WINDOW_ALGO"].toInt()
            options.debug = env["DEBUG"] == "true"
            options.quiet = env["QUIET"] == "true"
            return options
        }
    }
}