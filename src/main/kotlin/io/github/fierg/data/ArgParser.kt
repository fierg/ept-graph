package io.github.fierg.data

import io.github.fierg.model.options.CompositionMode
import io.github.fierg.model.options.DecompositionMode
import io.github.fierg.model.options.Options
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.optional

class ArgParser {
    companion object {
        fun readArgs(args: Array<String>): Options {
            val parser = ArgParser("EPT Graph Reader")

            val dotenv by parser.option(ArgType.Boolean, shortName = "env", description = "Use config from .env file (Recommended usage due to amount of args)").default(false)
            val input by parser.argument(ArgType.Int, description = "input (Network id in range (0..61)")
            val threshold by parser.argument(ArgType.Double, description = "Min threshold of cover to be valid").optional().default(1.0)
            val state by parser.option(ArgType.Boolean, description = "Invert state to substitute in decomposition (if set, decomposition will replace 0s instead of 1s)", shortName = "s").default(false)
            val decompositionMode by parser.option(ArgType.Choice<DecompositionMode>(), description = "Choose how a decomposition is found, using only maximal divisors, using all factors and greedily collect up to the threshold or perform a fourier transform for increased understandability.", fullName = "Mode of composing factors").default(DecompositionMode.GREEDY_SHORT_FACTORS)
            val compositionMode by parser.option(ArgType.Choice<CompositionMode>(), description = "Choose how a composition is formed, using AND or OR operator for adding factors together to a decomposition.", fullName = "Mode of composing factors").default(CompositionMode.OR)
            val skipSelfEdges by parser.option(ArgType.Boolean, shortName = "skipSelfEdges", description = "Skip loop back edges with same source and target, these are often useless.").default(false)
            val debug by parser.option(ArgType.Boolean, shortName = "d", description = "Turn on debug mode").default(false)
            val quiet by parser.option(ArgType.Boolean, shortName = "q", description = "Turn on quiet mode").default(false)
            val deltaWindowPreprocessing by parser.option(ArgType.Int, description = "Delta window in preprocessing of the label").default(0)
            val deltaWindowAlgo by parser.option(ArgType.Int, description = "Delta window during decomposing").default(0)

            parser.parse(args = args)

            return Options(dotenv, input, state, decompositionMode, compositionMode, skipSelfEdges, debug, quiet, deltaWindowPreprocessing, deltaWindowAlgo, threshold)
        }
    }
}