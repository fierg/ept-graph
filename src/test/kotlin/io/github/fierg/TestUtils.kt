package io.github.fierg

import io.github.fierg.algo.Decomposer
import io.github.fierg.data.F2FReader
import io.github.fierg.extensions.*
import io.github.fierg.model.result.Cover
import io.github.fierg.model.result.Factor
import io.github.fierg.periodic.Periodic
import org.junit.Test

class TestUtils {

    @Test
    fun testUtils1() {
        val array = arrayOf(true, false, false, true, true, false).toBooleanArray()
        val period = Periodic().findShortestPeriod(array)
        println("period is $period")

        assert(period == 6)
    }

    @Test
    fun testUtils2() {
        val number = 24
        val factors = number.factorsSequence().toList()
        println("Factors of $number are $factors")
        val expectedFactors = listOf(1, 2, 3, 4, 6, 8, 12, 24)

        assert(factors == expectedFactors)
    }

    @Test
    fun testUtils22() {
        val number = 28
        val factors = number.factorsSequence().toList()
        println("Factors of $number are $factors")
        assert(factors == listOf(1, 2, 4, 7, 14, 28))
    }

    @Test
    fun testUtils3() {
        val array = arrayOf(true, false, false, true, true, false).toBooleanArray()
        val periods = Decomposer(state = false, threshold = 0.5).findCover(array)
        val expectedFactors = listOf(Factor(arrayOf(false), listOf(0,3,4)),Factor(arrayOf(false,false), listOf(0,3,4)),Factor(arrayOf(true,false,false), listOf(4)))
        val expectedPeriods = Cover(3,3, listOf(4), listOf(true,false,false).toBooleanArray(), expectedFactors)
        assert(periods == expectedPeriods)
    }

    @Test
    fun testUtils3b() {
        val array = arrayOf(true, false, false, true, true, false).toBooleanArray()
        val periods = Decomposer(state = false).findCover(array)
        val expectedFactors = listOf(Factor(arrayOf(false), listOf(0,3,4)),Factor(arrayOf(false,false), listOf(0,3,4)),Factor(arrayOf(true,false,false), listOf(4)),Factor(arrayOf(true,false,false,true,true,false), listOf()))
        val expectedPeriods = Cover(3,6, emptyList(), listOf(true,false,false,true,true,false).toBooleanArray(),expectedFactors)

        assert(periods == expectedPeriods)
    }

    @Test
    fun testUtils4() {
        val cover = Decomposer(state = false).findCover(BooleanArray(16) {true})
        val expectedFactors = listOf(Factor(arrayOf(true), listOf()))
        assert(cover == Cover(16,1, emptyList(), BooleanArray(1){true}, expectedFactors))
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
        println("Factors of $number are $factors")
        assert(factors == listOf(1, 2, 3, 4, 5, 6, 10, 11, 12, 15, 20, 22, 30, 33, 44, 55, 60, 66, 110, 121, 132, 165, 220, 242, 330, 363, 484, 605, 660, 726, 1210, 1452, 1815, 2420, 3630, 7260))
    }

    @Test
    fun testPrimeFactors(){
        val number = 7260
        val primeFactors = number.primeFactors().toList()
        println("Prime factors are $primeFactors")
        assert(listOf(2, 2, 3, 5, 11, 11) == primeFactors)
    }

    @Test
    fun testMaximalDivisors(){
        val number = 7260
        val maximalDivisors = number.maximalDivisors().toList()
        println("Maximal Divisors are $maximalDivisors")
        assert(maximalDivisors == listOf(660, 1452, 2420, 3630))
    }

    @Test
    fun testRemoveOutliersMethod(){
        val listA = mutableListOf(1, 3, 5, 7, 9, 11)
        val listB = listOf(3, 5, 7, 10, 12)

        println("List A: $listA")
        println("List B: $listB")

        listA.removeIfNotIncludedIn(listB)

        println("Transformed A: $listA")
        assert(listA == listOf(3,5,7))
    }

    @Test
    fun getOutliersFalse() {
        val array = arrayOf(true, false, false, true, false, false).toBooleanArray()
        val cover = arrayOf(true, false, false, true, true, false).toBooleanArray()
        val outliers = Decomposer(state = true).getOutliers(array, cover = cover)
        val expectedOutliers = listOf(4)

        assert(outliers == expectedOutliers)
    }

    @Test
    fun getOutliersTrue() {
        val array = arrayOf(true, false, false, true, true, false).toBooleanArray()
        val cover = arrayOf(true, false, false, true, false, false).toBooleanArray()
        val outliers = Decomposer(state = false).getOutliers(array, cover = cover)
        val expectedOutliers = listOf(4)

        assert(outliers == expectedOutliers)
    }
}