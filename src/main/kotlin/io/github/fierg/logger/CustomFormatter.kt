package io.github.fierg.logger

import org.fusesource.jansi.Ansi
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.charset.Charset
import java.util.*
import java.util.logging.Formatter
import java.util.logging.LogRecord


class CustomFormatter : Formatter() {

    override fun format(record: LogRecord): String {
        val dat = Date()
        val message = formatMessage(record)

        return when (record.level.toString()) {
            "INFO" -> Ansi.ansi().fgDefault().a(String.format(format, dat, "","", record.level, message, )).reset().toString()
            "FINE" ->  Ansi.ansi().fgBlue().a(String.format(format, dat, "","", record.level, message)).reset().toString()
            "WARNING" ->  Ansi.ansi().fgBrightYellow().a(String.format(format, dat, "","", record.level, message)).reset().toString()
            "SEVERE" ->  Ansi.ansi().fgRed().a(String.format(format, dat, "","", record.level, message)).reset().toString()
            else ->  Ansi.ansi().fgDefault().a(String.format(format, dat,  "","", record.level, message)).reset().toString()
        }
    }

    companion object {
        val format = getLoggingFormat()

        private fun getLoggingFormat(): String {
            try {
                val loggingProperties = this::class.java.classLoader.getResource("logging.properties").readText(Charset.defaultCharset())
                val regex = Regex("^java.util.logging.CustomFormatter.format=(.+)\$")
                loggingProperties.lines().forEach { line ->
                    if (regex.matches(line)) return regex.find(line)!!.groupValues[1]
                }
            } catch (e:Exception) {
                println("\u001B[31mApplication could not find logging.properties!\u001B[0m")
            }

            return "[%1\$tF %1\$tT] [%4\$s] %5\$s %n"
        }
    }
}