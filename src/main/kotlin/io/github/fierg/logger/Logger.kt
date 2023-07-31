package io.github.fierg.logger

import org.fusesource.jansi.AnsiConsole
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger

object Logger {
    private var LOGGER: Logger? = null

    init {
        try {
            LogManager.getLogManager().readConfiguration(this.javaClass.classLoader.getResource("logging.properties").openStream())
        } catch (ex: Exception) {
            Logger.getLogger(Logger::class.java.name).log(Level.SEVERE, "Failed to read logging.properties file", ex)
        }
        LOGGER = Logger.getLogger(Logger::class.java.name)
        AnsiConsole.systemInstall()
    }

    fun debug(msg:String) {
        LOGGER!!.log(Level.FINE, msg)
    }

    fun info(msg:String) {
        LOGGER!!.log(Level.INFO, msg)
    }

    fun error(msg:String) {
        LOGGER!!.log(Level.SEVERE, msg)
    }

    fun warn(msg:String) {
        LOGGER!!.log(Level.WARNING, msg)
    }

    fun setLogLevelToDebug() {
        LOGGER!!.level = Level.FINE
    }

    fun setLogLevelToQuiet() {
        LOGGER!!.level = Level.OFF
    }
}