package io.github.fierg.model

data class Options(
    var dotenv: Boolean,
    var input: Int,
    var state: Boolean,
    var coroutines: Boolean,
    var clean: Boolean,
    var mode: CompositionMode?,
    var skipSingleStepEdges: Boolean,
    var debug: Boolean,
    var quiet: Boolean,
    var deltaWindowPreprocessing: Int,
    var deltaWindowAlgo: Int
)