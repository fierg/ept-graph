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
){
    companion object{
        fun emptyOptions(): Options {
            return Options(false,0,
                state = false,
                coroutines = false,
                clean = false,
                mode = CompositionMode.ALL,
                skipSingleStepEdges = false,
                debug = false,
                quiet = false,
                deltaWindowPreprocessing = 0,
                deltaWindowAlgo = 0
            )
        }
    }
}