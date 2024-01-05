package io.github.fierg.model.options

data class Options(
    var dotenv: Boolean,
    var input: Int,
    var decompositionMode: DecompositionMode,
    var compositionMode: CompositionMode,
    var skipSelfEdges: Boolean,
    var debug: Boolean,
    var quiet: Boolean,
    var deltaWindowPreprocessing: Int,
    var deltaWindowAlgo: Int,
    var threshold: Double,
    var allowFullLengthDecomposition: Boolean
) {
    companion object {
        fun emptyOptions(): Options {
            return Options(
                false,
                0,
                decompositionMode = DecompositionMode.GREEDY_SHORT_FACTORS,
                compositionMode = CompositionMode.OR,
                skipSelfEdges = true,
                debug = false,
                quiet = false,
                deltaWindowPreprocessing = 0,
                deltaWindowAlgo = 0,
                threshold = 1.0,
                allowFullLengthDecomposition = false
            )
        }

        fun getDeltaSuit(): List<Options> {
            val orMaxD1 = emptyOptions()
            orMaxD1.decompositionMode = DecompositionMode.MAX_DIVISORS
            orMaxD1.deltaWindowAlgo = 1

            val orGreedyD1 = emptyOptions()
            orGreedyD1.deltaWindowAlgo = 1

            val orFourierD1 = emptyOptions()
            orFourierD1.decompositionMode = DecompositionMode.FOURIER_TRANSFORM
            orFourierD1.deltaWindowAlgo = 1

            return listOf(
                orMaxD1, orGreedyD1, orFourierD1
            )
        }

        fun getDefaultSuit(): List<Options> {
            val orMax = emptyOptions()
            orMax.decompositionMode = DecompositionMode.MAX_DIVISORS

            val orGreedy = emptyOptions()

            val orFourier = emptyOptions()
            orFourier.decompositionMode = DecompositionMode.FOURIER_TRANSFORM

            val andMax = emptyOptions()
            andMax.decompositionMode = DecompositionMode.MAX_DIVISORS
            andMax.compositionMode = CompositionMode.AND

            val andGreedy = emptyOptions()
            andGreedy.compositionMode = CompositionMode.AND

            return listOf(
                orMax, andMax, orGreedy, andGreedy, orFourier
            )
        }

        fun getPrecisionSuit() : List<Options> {
            val orMax = emptyOptions()
            orMax.decompositionMode = DecompositionMode.MAX_DIVISORS

            val andMax = emptyOptions()
            andMax.decompositionMode = DecompositionMode.MAX_DIVISORS
            andMax.compositionMode = CompositionMode.AND

            val orMaxD1 = emptyOptions()
            orMaxD1.decompositionMode = DecompositionMode.GREEDY_SHORT_FACTORS
            orMaxD1.deltaWindowAlgo = 1

            val orMaxD2 = emptyOptions()
            orMaxD2.decompositionMode = DecompositionMode.GREEDY_SHORT_FACTORS
            orMaxD2.deltaWindowAlgo = 2

            return  listOf(
                orMax, andMax, orMaxD1, orMaxD2
            )
        }
    }
}