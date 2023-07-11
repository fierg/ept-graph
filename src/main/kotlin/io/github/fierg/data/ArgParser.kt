package io.github.fierg.data

import io.github.fierg.model.options.CompositionMode
import io.github.fierg.model.options.Options
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default

class ArgParser {

    companion object {
        fun readArgs(args: Array<String>): Options {
            val parser = ArgParser("EPT Graph Reader")

            val dotenv by parser.option(ArgType.Boolean, shortName = "env", description = "Use config from .env file (Recommended usage due to amount of args)").default(false)
            val input by parser.argument(ArgType.Int, description = "input (Network id in range (0..61)")
            val state by parser.option(ArgType.Boolean, description = "Invert state to substitute in decomposition (if set, decomposition will replace 0s instead of 1s)", shortName = "s").default(false)
            val coroutines by parser.option(ArgType.Boolean, description = "Use Coroutines for period computation. (Use with check)", shortName = "co").default(false)
            val clean by parser.option(ArgType.Boolean, description = "Clean up periods of multiples", shortName = "cl").default(false)
            val mode by parser.option(ArgType.Choice<CompositionMode>(), shortName = "m", description = "Mode of composing the periods [ALL,SIMPLE,GREEDY,SET_COVER_ILP]")
            val skipSingleStepEdges by parser.option(ArgType.Boolean, shortName = "skipSingle", description = "Skip single time step label edges").default(false)
            val debug by parser.option(ArgType.Boolean, shortName = "d", description = "Turn on debug mode").default(false)
            val quiet by parser.option(ArgType.Boolean, shortName = "q", description = "Turn on quiet mode").default(false)
            val deltaWindowPreprocessing by parser.option(ArgType.Int, description = "Delta window in preprocessing of the label").default(0)
            val deltaWindowAlgo by parser.option(ArgType.Int, description = "Delta window during decomposing").default(0)

            parser.parse(args = args)

            return Options(dotenv, input, state, coroutines, clean, mode, skipSingleStepEdges, debug, quiet, deltaWindowPreprocessing, deltaWindowAlgo)
        }
    }
}