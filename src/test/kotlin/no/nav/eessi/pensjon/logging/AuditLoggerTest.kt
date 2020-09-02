package no.nav.eessi.pensjon.logging

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import com.nhaarman.mockitokotlin2.atLeastOnce
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
        auditLogger.cefLog(mapOf(AuditLogger.AuditKey.BRUKERIDENT to "Z990652", AuditLogger.AuditKey.TJENESTEN to "addInstitutionAndDocument", AuditLogger.AuditKey.BORGERFNR to "15268923561", AuditLogger.AuditKey.AKTOER to "31242"))

        verify(mockedAppender, atLeastOnce()).doAppend(argumentCaptor.capture());
        val logEvent = argumentCaptor.getValue();

        Assertions.assertTrue(logEvent.getMessage().contains("CEF:0|EESSI|EESSI-PENSJON|Audit:accessed|AuditLog|INFO"))
        Assertions.assertTrue(logEvent.getMessage().contains("suid=Z990652 duid=15268923561 aktoer=31242 flexString1=addInstitutionAndDocument flexString1Label=tjenesten flexString2= flexString2Label=error flexString3= flexString3Label=euxData"))

    }

    @Test
    fun testLogBucDeprecated() {
        auditLogger.logBuc("OpprettBuc", "123456")

        verify(mockedAppender, atLeastOnce()).doAppend(argumentCaptor.capture())
        val logEvent = argumentCaptor.getValue()

        println(logEvent.message)

        Assertions.assertTrue(logEvent.getMessage().contains("CEF:0|EESSI|EESSI-PENSJON|Audit:accessed|AuditLog|INFO"))
        Assertions.assertTrue(logEvent.getMessage().contains("suid=n/a duid= aktoer= flexString1=OpprettBuc flexString1Label=tjenesten flexString2= flexString2Label=error flexString3=123456 flexString3Label=euxData"))

    }


//    CEF:0|EESSI-PENSJON|AUDIT|1.0|||INFO|brukerident=n/a tjenesten=addInstutionAndDocument borgerfnr= aktoer=0105094340092 error= euxdata= requestcontext=sakId: EESSI-PEN-123 vedtakId: 1234567 buc: P_BUC_06 sed: P6000 euxCaseId: 1234567890

    @Test
    fun `Test logging av requestcontext`() {
        auditLogger.log("OpprettBuc", "123456", "sakId: EESSI-PEN-123 vedtakId: 1234567 buc: P_BUC_06 sed: P6000 euxCaseId: 1234567890")

        verify(mockedAppender, atLeastOnce()).doAppend(argumentCaptor.capture())
        val logEvent = argumentCaptor.getValue()

        Assertions.assertTrue(logEvent.message.contains("CEF:0|EESSI|EESSI-PENSJON|Audit:accessed|AuditLog|INFO"))
        Assertions.assertTrue(logEvent.message.contains("suid=n/a duid= aktoer=123456 flexString1=OpprettBuc flexString1Label=tjenesten flexString2= flexString2Label=error flexString3= flexString3Label="))

        Assertions.assertTrue(logEvent.message.contains("deviceCustomString1=EESSI-PEN-123 deviceCustomString1Label=sakId deviceCustomString2=1234567 deviceCustomString2Label=vedtakId deviceCustomString3=P_BUC_06 deviceCustomString3Label=buc deviceCustomString4=P6000 deviceCustomString4Label=sed deviceCustomString5=1234567890 deviceCustomString5Label=euxCaseId"))

    }
}



