package io.github.fierg

import io.github.fierg.data.F2FReader
import io.github.fierg.extensions.*
import io.github.fierg.logger.Logger
import io.github.fierg.model.result.Cover
import org.junit.Test

class TestUtils {

    @Test
    fun testUtils2() {
        val number = 24
        val factors = number.factorsSequence().toList()
        Logger.info("Factors of $number are $factors")
        val expectedFactors = listOf(1, 2, 3, 4, 6, 8, 12, 24)

        assert(factors == expectedFactors)
    }

    @Test
    fun testUtils22() {
        val number = 28
        val factors = number.factorsSequence().toList()
        Logger.info("Factors of $number are $factors")
        assert(factors == listOf(1, 2, 4, 7, 14, 28))
    }

    @Test
    fun testUtils10(){
        val b1 = BooleanArray(8) {false}
        val b2 = BooleanArray(2) {false}
        b2[1] = true
        val result = BooleanArray(8)
        result[1] = true
        result[3] = true
        result[5] = true
        result[7] = true
        b1.applyPeriod(b2, true)

        assert(b1.contentEquals(result))
    }

    @Test
    fun testUtils11() {
        val f2fGraph = F2FReader().getF2FNetwork(0)
        f2fGraph.getGraphAtStep(2)
        f2fGraph.getGraphAtStep(4)
        f2fGraph.getGraphAtStep(6)
    }

    @Test
    fun testFactors(){
        val number = 7260
        val factors = number.factorsSequence().toList()
        Logger.info("Factors of $number are $factors")
        assert(factors == listOf(1, 2, 3, 4, 5, 6, 10, 11, 12, 15, 20, 22, 30, 33, 44, 55, 60, 66, 110, 121, 132, 165, 220, 242, 330, 363, 484, 605, 660, 726, 1210, 1452, 1815, 2420, 3630, 7260))
    }

    @Test
    fun testPrimeFactors(){
        val number = 7260
        val primeFactors = number.primeFactors().toList()
        Logger.info("Prime factors are $primeFactors")
        assert(listOf(2, 2, 3, 5, 11, 11) == primeFactors)
    }

    @Test
    fun testMaximalDivisors(){
        val number = 7260
        val maximalDivisors = number.maximalDivisors().toList()
        Logger.info("Maximal Divisors are $maximalDivisors")
        assert(maximalDivisors == listOf(660, 1452, 2420, 3630))
    }

    @Test
    fun testRemoveOutliersMethod(){
        val listA = mutableListOf(1, 3, 5, 7, 9, 11)
        val listB = listOf(3, 5, 7, 10, 12)

        Logger.info("List A: $listA")
        Logger.info("List B: $listB")

        listA.removeIfNotIncludedIn(listB)

        Logger.info("Transformed A: $listA")
        assert(listA == listOf(3,5,7))
    }

    @Test
    fun cleanFactor(){
        val array1 = arrayOf(true, false).toBooleanArray()
        val array2 = arrayOf(true, false, true, false, true, false, true, true).toBooleanArray()

        val cover = Cover(array1, true)
        val result = cover.cleanFactor(array1,array2)
        assert(result.contentEquals(arrayOf(false, false, false, false, false, false, false, true).toBooleanArray()))
    }
}