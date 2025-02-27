package no.nav.eessi.pensjon.logging.appender

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.util.LevelToSyslogSeverity
import ch.qos.logback.core.AppenderBase
import ch.qos.logback.core.Layout
import org.productivity.java.syslog4j.SyslogConfigIF
import org.productivity.java.syslog4j.SyslogIF
import org.productivity.java.syslog4j.SyslogRuntimeException

/**
 * Dette er en kopi av Syslog4jAppender fra https://github.com/papertrail/logback-syslog4j
 * konvertert til kotlin og som en del av en jobb for å oppaterer logback til nyere versjon
 */
class Syslog4jAppender<E> : AppenderBase<E>() {
    private var syslog: SyslogIF? = null
    var syslogConfig: SyslogConfigIF? = null
    var layout: Layout<E>? = null

    override fun append(loggingEvent: E) {
        syslog?.log(getSeverityForEvent(loggingEvent.toString()), layout?.doLayout(loggingEvent))
    }

    override fun start() {
        super.start()
        synchronized(this) {
            try {
                val syslogClass = syslogConfig?.syslogClass
                syslog = syslogClass?.getDeclaredConstructor()?.newInstance() as SyslogIF
                syslog?.initialize(syslogClass.simpleName, syslogConfig)
            } catch (cse: ClassCastException) {
                throw SyslogRuntimeException(cse)
            } catch (iae: IllegalAccessException) {
                throw SyslogRuntimeException(iae)
            } catch (ie: InstantiationException) {
                throw SyslogRuntimeException(ie)
            }
        }
    }

    override fun stop() {
        super.stop()
        synchronized(this) {
            syslog?.shutdown()
            syslog = null
        }
    }
    fun getSeverityForEvent(eventObject: Any): Int {
        return if (eventObject is ILoggingEvent) {
            LevelToSyslogSeverity.convert(eventObject)
        } else {
            SyslogIF.LEVEL_INFO
        }
    }
}