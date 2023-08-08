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

            Logger.debug("Parsing args from .env file:\n${env.entries().filter { envEntry -> ENV.values().map { envVar -> envVar.toString() }.contains(envEntry.key) }.map { "${it.key}:${it.value}\n" }}")

            options.state = env[ENV.STATE.name] == "true"
            options.skipSelfEdges = env[ENV.SKIP_SINGLE_STEP_EDGES.name] == "true"
            options.deltaWindowPreprocessing = env[ENV.DELTA_WINDOW_PREPROCESSING.name].toInt()
            options.deltaWindowAlgo = env[ENV.DELTA_WINDOW_ALGO.name].toInt()
            options.debug = env[ENV.DEBUG.name] == "true"
            options.quiet = env[ENV.QUIET.name] == "true"
            options.threshold = env[ENV.THRESHOLD.name].toDouble()

            options.compositionMode = when (env[ENV.MODE.name]) {
                CompositionMode.FOURIER_TRANSFORM.name -> CompositionMode.FOURIER_TRANSFORM
                CompositionMode.SHORTEST_PERIODS.name -> CompositionMode.SHORTEST_PERIODS
                CompositionMode.MAX_DIVISORS.name -> CompositionMode.MAX_DIVISORS
                else -> {
                    Logger.warn("Composition Mode missing! Running with ${CompositionMode.SHORTEST_PERIODS.name}")
                    CompositionMode.SHORTEST_PERIODS
                }
            }

            return options
        }
    }
}