package io.github.fierg.data

import io.github.cdimascio.dotenv.dotenv
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.CompositionMode
import io.github.fierg.model.options.ENV
import io.github.fierg.model.options.Options

class DotEnvParser {
    companion object{
        fun readDotEnv(options: Options = Options.emptyOptions()): Options {
            val env = dotenv {
                directory = "./"
                ignoreIfMalformed = true
                ignoreIfMissing = true
                systemProperties = true
            }
            Logger.info("Parsing arg from .env file:\n${env.entries().filter { envEntry -> ENV.values().map { envVar -> envVar.toString() }.contains(envEntry.key) }.map { "${it.key}:${it.value}\n" }}")
            options.state = env[ENV.STATE.name] == "true"
            options.skipSingleStepEdges = env[ENV.SKIP_SINGLE_STEP_EDGES.name] == "true"

            options.compositionMode = when (env[ENV.MODE.name]) {
                "GREEDY" -> CompositionMode.GREEDY
                "SHORTEST_PERIODS" -> CompositionMode.SHORTEST_PERIODS
                "MAX_DIVISORS" -> CompositionMode.MAX_DIVISORS
                else -> {
                    Logger.warn("Composition Mode missing! Running with SHORTEST_PERIODS")
                    CompositionMode.SHORTEST_PERIODS
                }
            }
            options.deltaWindowPreprocessing = env[ENV.DELTA_WINDOW_PREPROCESSING.name].toInt()
            options.deltaWindowAlgo = env[ENV.DELTA_WINDOW_ALGO.name].toInt()
            options.debug = env[ENV.DEBUG.name] == "true"
            options.quiet = env[ENV.QUIET.name] == "true"
            options.threshold = env[ENV.THRESHOLD.name].toDouble()
            return options
        }
    }
}