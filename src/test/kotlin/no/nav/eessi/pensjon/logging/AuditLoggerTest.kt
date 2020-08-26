package no.nav.eessi.pensjon.logging

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.slf4j.LoggerFactory.getLogger


@ExtendWith(MockitoExtension::class)
class AuditLoggerTest {

    @Mock
    lateinit var mocktoken: TokenValidationContextHolder

    @Mock
    lateinit var mockedAppender: Appender<ILoggingEvent>

    @Captor
    lateinit var argumentCaptor: ArgumentCaptor<ILoggingEvent>

    lateinit var auditLogger: AuditLogger

    @BeforeEach
    fun setup() {
        auditLogger = AuditLogger(mocktoken)
        val logger : Logger = getLogger("auditLogger") as Logger
        logger.addAppender(mockedAppender)
    }

    @AfterEach
    fun takedown() {
        val logger : Logger = getLogger("auditLogger") as Logger
        logger.detachAppender(mockedAppender)
    }

    @Test
    fun testOne() {
        auditLogger.cefLog(mapOf(AuditLogger.AuditKey.TJENESTEN to "12312", AuditLogger.AuditKey.AKTOER to "31242"))

        verify(mockedAppender).doAppend(argumentCaptor.capture());
        val logEvent = argumentCaptor.getValue();

        Assertions.assertTrue(logEvent.getMessage().contains("CEF:0|EESSI-PENSJON|AUDIT|1.0|||INFO|"))
        Assertions.assertTrue(logEvent.getMessage().contains("brukerident= tjenesten=12312 borgerfnr= aktoer=31242 error= euxdata= requestcontext="))

    }

}