package io.github.fierg

import io.github.fierg.algo.Decomposition
import io.github.fierg.data.FileReader
import io.github.fierg.logger.Logger
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default


fun main(args: Array<String>){

    val parser = ArgParser("EPT Graph Reader")

    val input by parser.argument(ArgType.Int, description = "input (Network id in range (0..61)")
    val state by parser.option(ArgType.Boolean, description = "State to substitute in decomposition (true,false)", shortName = "s").default(false)
    val debug by parser.option(ArgType.Boolean, shortName = "d", description = "Turn on debug mode").default(false)
    val quiet by parser.option(ArgType.Boolean, shortName = "q", description = "Turn on quiet mode").default(false)

    parser.parse(args = args)

    if (debug){
        Logger.setLogLevelToDebug()
    }

    if (quiet){
        Logger.setLogLevelToQuiet()
    }

    val f2fGraph = FileReader().getF2FNetwork(input)
    Decomposition(state).findComposite(f2fGraph)

}