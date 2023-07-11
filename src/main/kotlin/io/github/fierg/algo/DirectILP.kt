package io.github.fierg.algo

import gurobi.*
import io.github.fierg.logger.Logger
import kotlin.system.measureTimeMillis

class DirectILP {
    var solution: List<Pair<Int, Int>>? = null
    var ilpSolveTime = -1L

    fun solveInstanceWithILP(array: BooleanArray, periods: List<Pair<Int, Int>>): Pair<Int, List<Pair<Int, Int>>?> {
        Logger.info("Creating model for ILP for applying periods optimally ...")

        // Create empty environment , set options , and start
        val env = GRBEnv("kra-ilp.log")
        env.set(GRB.IntParam.LogToConsole, 0)
        env.start()
        // Create empty model
        val model = GRBModel(env)

        try {
            // Create variables
            val vars = mutableMapOf<Pair<Int, Int>, GRBVar>()
            createVariables(periods, vars, model)

            //Add constraint cover == array
            addConstraint(vars, model, array)

            // Set objective : min amount of periods
            addObjectiveFunction(vars, model)

            // Optimize model
            Logger.info("Solving instance with ILP ...")
            val ilpSolveTime = measureTimeMillis {
                model.optimize()
            }

            Logger.info("Obj : " + model.get(GRB.DoubleAttr.ObjVal))
            val result = model.get(GRB.DoubleAttr.ObjVal)

            backTrackSolution(vars)
            Logger.info("KRA Instance solved. Solution: $solution")

            // Dispose of model and environment
            model.dispose()
            env.dispose()

            return Pair(result.toInt(), solution)

        } catch (e: GRBException) {
            Logger.error(" Error code : " + e.errorCode.toString() + ". " + e.message)
            if (model.get(GRB.IntAttr.Status) == 3) {
                Logger.error("Model status = 3 -> model INFEASIBLE")
                model.computeIIS()
                model.write("IrreducibleInconsistentSubsystem.ilp")
            }
        }
        return Pair(-1, listOf())
    }

    private fun backTrackSolution(vars: Map<Pair<Int, Int>, GRBVar>) {
        TODO("Not yet implemented")
    }

    private fun addObjectiveFunction(vars: Map<Pair<Int, Int>, GRBVar>, model: GRBModel) {
        Logger.info("Adding objective function ...")
        val expr = GRBLinExpr()
        vars.forEach { (period, _) ->
            expr.addTerm(1.0, vars[period])
        }
        model.setObjective(expr, GRB.MINIMIZE)
    }

    private fun addConstraint(vars: Map<Pair<Int, Int>, GRBVar>, model: GRBModel, array: BooleanArray) {
        //TODO: set of periods has to fully cover the array
    }

    private fun createVariables(periods: List<Pair<Int, Int>>, vars: MutableMap<Pair<Int, Int>, GRBVar>, model: GRBModel) {
        Logger.info("Adding variables to model ($periods) ...")

        periods.forEachIndexed { index, period ->
            val variable = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x$index")
            vars[period] = variable
        }
    }
}