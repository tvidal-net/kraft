package uk.tvidal.kraft

import joptsimple.OptionParser
import joptsimple.OptionSet
import java.lang.System.getProperty
import java.lang.System.setProperty

object LogConfig {

    private const val LEVEL = "logback.level"
    var level: String
        get() = getProperty(LEVEL)
        set(value) {
            setProperty(LEVEL, value)
        }

    private const val CONFIG_FILE = "logback.configurationFile"
    var configurationFile: String
        get() = getProperty(CONFIG_FILE)
        set(value) {
            setProperty(CONFIG_FILE, value)
        }

    private const val APPENDER = "logback.appender"
    var appender: String
        get() = getProperty(APPENDER)
        set(value) {
            setProperty(APPENDER, value)
        }

    private const val LOG_FILE = "logback.logFile"
    var logFile: String
        get() = getProperty(LOG_FILE)
        set(value) {
            setProperty(LOG_FILE, value)
        }

    operator fun invoke(parser: OptionParser) {
        parser.stringArgument("log level", "log-level").defaultsTo("DEBUG")
        parser.stringArgument("log file", "log-file").defaultsTo("NOP")
    }

    operator fun invoke(op: OptionSet) {
        level = op.valueOf("log-level").toString()
        val file = op.valueOf("log-file").toString()
        when (file) {
            "NOP", "STDOUT" -> appender = file
            else -> {
                appender = "FILE"
                logFile = file
            }
        }
    }
}
