package io.github.fierg.model.result

/**
 * The `Factor` class represents a factor with a set of boolean values (cover) and a list of outliers.
 * It is a subclass of the `CleanQuotient` class, inheriting its properties and methods.
 *
 * @param cover     The array of boolean values representing the factor.
 * @param outliers  The list of indices representing outliers in the factor.
 */
class Factor(var cover: BooleanArray, val outliers: List<Int>) : CleanQuotient(cover) {
    /**
     * Constructs a `Factor` object using a boolean array to represent the cover and a list of outliers.
     *
     * @param cover     The array of boolean values representing the factor.
     * @param outliers  The list of indices representing outliers in the factor.
     */
    constructor(cover: Array<Boolean>, outliers: List<Int>) : this(cover = cover.toBooleanArray(), outliers)

    companion object {

        /**
         * Static method to calculate outliers for clean quotients using the provided parameters.
         *
         * @param target          The boolean array representing the target state.
         * @param stateToReplace  The boolean value to replace in the factors.
         * @param factors         A collection of boolean arrays representing factors.
         * @return A mutable list of integer indices representing outliers.
         */
        fun getOutliersForCleanQuotients(target: BooleanArray, stateToReplace: Boolean, factors: Collection<BooleanArray>): MutableList<Int> {
            val outliers = mutableListOf<Int>()
            target.forEachIndexed { index, state ->
                if (state == stateToReplace) {
                    if (factors.any { factor -> factor[index % factor.size] != stateToReplace })
                        outliers.add(index)
                } else {
                    if (factors.all { factor -> factor[index % factor.size] == stateToReplace })
                        outliers.add(index)
                }
            }
            return outliers
        }
    }

    /**
     * Returns a string representation of the `Factor` object, displaying the cover as a list of 0s and 1s.
     *
     * @return A string representation of the `Factor` object.
     */
    override fun toString(): String {
        return "${cover.map { if (it) "1" else "0" }}"
        //return "${cover.size}:${outliers.size}"
    }

    /**
     * Retrieves the boolean value at the specified index in the cover array.
     *
     * @param index The index of the element to retrieve.
     * @return The boolean value at the specified index in the cover array.
     */
    fun get(index: Int): Boolean {
        return cover[index % cover.size]
    }
}
