package io.github.fierg.algo

import gurobi.*
import io.github.fierg.logger.Logger
import kotlin.system.measureTimeMillis


class SetCoverILP(private val state: Boolean) {

    private var universe: Set<Int>? = null
    private var subSets: Set<Set<Int>>? = null
    var subSetMap: Map<Set<Int>,Pair<Int,Int>>? = null


    fun getSetCoverInstanceFromPeriods(periods: List<Pair<Int, Int>>, array: BooleanArray) {
        universe = getUniverseFromArray(array)
        subSetMap = getSubsetsFromPeriods(periods, array)
        subSets = subSetMap!!.keys
    }

    private fun getUniverseFromArray(array: BooleanArray): Set<Int> {
        val indices = mutableSetOf<Int>()

        for (i in array.indices) {
            if (array[i] == state) {
                indices.add(i)
            }
        }

        return indices
    }

    private fun getSubsetsFromPeriods(periods: List<Pair<Int, Int>>, array: BooleanArray) = periods.associateBy { periodToIndices(it, array.size) }


    private fun periodToIndices(period: Pair<Int, Int>, arraySize: Int): Set<Int> {
        val set = mutableSetOf<Int>()
        var index = period.first

        do {
            set.add(index)
            index = (index + period.second) % arraySize
        } while (index != period.first)

        return set
    }

    fun solveSetCover(): Set<Set<Int>> {
        try {
            // Create empty environment , set options , and start
            val env = GRBEnv("kra-ilp.log")
            env.set(GRB.IntParam.LogToConsole, 0)
            env.start()

            val model = GRBModel(env)

            if (subSets == null || universe == null){
                Logger.error("Universe or subsets is null!")
                return emptySet()
            }

            // Decision variables
            val vars = mutableMapOf<Set<Int>, GRBVar>()
            createVariables(vars, model)

            // Objective function: minimize the number of sets used
            addObjectiveFunction(vars, model)

            // Constraints: each element should be covered by at least one set
            addConstraints(vars, model)

            // Solve the model
            val ilpSolveTime = measureTimeMillis {
                model.optimize()
            }
            val chosenSets = backtrack(vars)


            // Dispose of the model and environment
            model.dispose()
            env.dispose()

            return chosenSets
        } catch (e: GRBException) {
            e.printStackTrace()
            return emptySet()
        }
    }

    private fun backtrack(vars: MutableMap<Set<Int>, GRBVar>): MutableSet<Set<Int>> {
        // Get the solution
        val chosenSets = mutableSetOf<Set<Int>>()
        subSets!!.forEach {
            if (vars[it]!![GRB.DoubleAttr.X] > 0.5) {
                chosenSets.add(it)
            }
        }
        return chosenSets
    }

    private fun addConstraints(vars: MutableMap<Set<Int>, GRBVar>, model: GRBModel) {
        for (element in universe!!) {
            val expr = GRBLinExpr()
            subSets!!.forEach { subset ->
                if (subset.contains(element)) {
                    expr.addTerm(1.0, vars[subset])
                }
            }
            model.addConstr(expr, GRB.GREATER_EQUAL, 1.0, "cover$element")
        }
    }

    private fun addObjectiveFunction(vars: MutableMap<Set<Int>, GRBVar>, model: GRBModel) {
        val objective = GRBLinExpr()
        subSets!!.forEach {
            objective.addTerm(1.0, vars[it])
        }
        model.setObjective(objective, GRB.MINIMIZE)
    }

    private fun createVariables(setVars: MutableMap<Set<Int>, GRBVar>, model: GRBModel) {
        var index = 0
        subSets!!.forEach {
            setVars[it] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "set${index++}")
        }
    }
}