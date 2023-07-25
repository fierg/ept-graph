package io.github.fierg.data

import io.github.cdimascio.dotenv.dotenv
import io.github.fierg.logger.Logger
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
            options.state = env["STATE"] == "true"
            options.skipSingleStepEdges = env["SKIP_SINGLE_STEP_EDGES"] == "true"
            options.deltaWindowPreprocessing = env["DELTA_WINDOW_PREPROCESSING"].toInt()
            options.deltaWindowAlgo = env["DELTA_WINDOW_ALGO"].toInt()
            options.debug = env["DEBUG"] == "true"
            options.quiet = env["QUIET"] == "true"
            options.threshold = env["THRESHOLD"].toDouble()
            return options
        }
    }
}