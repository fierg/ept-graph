package io.github.fierg.data

import io.github.cdimascio.dotenv.dotenv
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.CompositionMode
import io.github.fierg.model.options.DecompositionMode
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

            options.decompositionMode = when (env[ENV.DECOMPOSITION_MODE.name]) {
                DecompositionMode.FOURIER_TRANSFORM.name -> DecompositionMode.FOURIER_TRANSFORM
                DecompositionMode.GREEDY_SHORT_FACTORS.name -> DecompositionMode.GREEDY_SHORT_FACTORS
                DecompositionMode.MAX_DIVISORS.name -> DecompositionMode.MAX_DIVISORS
                else -> {
                    if (env[ENV.DECOMPOSITION_MODE.name].isNullOrEmpty()) {
                        Logger.warn("Decomposition Mode missing! Running with ${DecompositionMode.GREEDY_SHORT_FACTORS.name}")
                        DecompositionMode.GREEDY_SHORT_FACTORS
                    } else {
                        Logger.warn("Decomposition Mode ${env[ENV.DECOMPOSITION_MODE.name]} not recognized! Running with ${DecompositionMode.GREEDY_SHORT_FACTORS.name}")
                        DecompositionMode.GREEDY_SHORT_FACTORS
                    }
                }
            }

            options.compositionMode = when (env[ENV.COMPOSITION_MODE.name]) {
                CompositionMode.OR.name -> CompositionMode.OR
                CompositionMode.AND.name -> CompositionMode.AND
                else -> {
                    Logger.warn("Composition mode ${env[ENV.COMPOSITION_MODE.name]} not recognized! Running with ${CompositionMode.OR.name}")
                    CompositionMode.OR
                }
            }

            return options
        }
    }
}