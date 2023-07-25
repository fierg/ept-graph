package io.github.fierg.model.options

data class Options(
    var dotenv: Boolean,
    var input: Int,
    var state: Boolean,
    var skipSingleStepEdges: Boolean,
    var debug: Boolean,
    var quiet: Boolean,
    var deltaWindowPreprocessing: Int,
    var deltaWindowAlgo: Int,
    var threshold: Double
){
    companion object{
        fun emptyOptions(): Options {
            return Options(false,0,
                state = false,
                skipSingleStepEdges = false,
                debug = false,
                quiet = false,
                deltaWindowPreprocessing = 0,
                deltaWindowAlgo = 0,
                threshold = 1.0
            )
        }
    }
}