package no.nav.eessi.pensjon.logging

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import com.nhaarman.mockitokotlin2.atLeastOnce
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.slf4j.LoggerFactory.getLogger
import kotlin.math.log


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
        val logger: Logger = getLogger("auditLogger") as Logger
        logger.addAppender(mockedAppender)
    }

    @AfterEach
    fun takedown() {
        val logger: Logger = getLogger("auditLogger") as Logger
        logger.detachAppender(mockedAppender)
    }

    @Test
    fun `tester ut nytt loggformat cef`() {
        auditLogger.cefLog(mapOf(AuditKey.BRUKERIDENT to "Z990652", AuditKey.TJENESTEN to "addInstitutionAndDocument",AuditKey.AKTOER to "31242"))

        verify(mockedAppender, atLeastOnce()).doAppend(argumentCaptor.capture());
        val logEvent = argumentCaptor.getValue();

        assertTrue(logEvent.getMessage().contains("CEF:0|EESSI|EESSI-PENSJON|1.0|Audit:accessed|AuditLog|INFO"))
        assertTrue(logEvent.getMessage().contains("suid=Z990652 duid=31242 cs3=addInstitutionAndDocument cs3Label=tjenesten"))

    }

    @Test
    fun testLogBuc() {
        auditLogger.logBuc("OpprettBuc", "123456")

        verify(mockedAppender, atLeastOnce()).doAppend(argumentCaptor.capture())
        val logEvent = argumentCaptor.getValue()

        assertTrue(logEvent.getMessage().contains("CEF:0|EESSI|EESSI-PENSJON|1.0|Audit:accessed|AuditLog|INFO"))
        assertTrue(logEvent.getMessage().contains("suid=n/a cs3=OpprettBuc cs3Label=tjenesten cs5=euxCaseId:123456"))

    }

    @Test
    fun `logbuc med tjenestenavn alldoc`() {
        auditLogger.logBuc("getAllDocuments", "123456")

        verify(mockedAppender, atLeastOnce()).doAppend(argumentCaptor.capture())
        val logEvent = argumentCaptor.getValue()

        assertTrue(logEvent.getMessage().contains("CEF:0|EESSI|EESSI-PENSJON|1.0|Audit:accessed|AuditLog|INFO"))
        assertTrue(logEvent.getMessage().contains("suid=n/a cs3=getAllDocuments cs3Label=tjenesten cs5=euxCaseId:123456"))

    }


    @Test
    fun `test av log kun funksjonnavn i log (getdocuments i fagmodul)`() {
        auditLogger.log("buc")

        verify(mockedAppender, atLeastOnce()).doAppend(argumentCaptor.capture())
        val logEvent = argumentCaptor.getValue()

        assertTrue(logEvent.getMessage().contains("CEF:0|EESSI|EESSI-PENSJON|1.0|Audit:accessed|AuditLog|INFO"))
        assertTrue(logEvent.getMessage().contains("suid=n/a cs3=buc cs3Label=tjenesten"))

    }

    @Test
    fun `Test av logging av PrefillP6000 med context`() {
        auditLogger.log("confirmDocument", "0105094340092", "sakId: 22874955, vedtakId: 9876543211, buc: P_BUC_02, sed: P6000, euxCaseId: 123123")

        verify(mockedAppender, atLeastOnce()).doAppend(argumentCaptor.capture())
        val logEvent = argumentCaptor.getValue()

        assertTrue(logEvent.message.contains("CEF:0|EESSI|EESSI-PENSJON|1.0|Audit:accessed|AuditLog|INFO"))
        assertTrue(logEvent.message.contains("suid=n/a duid=0105094340092 cs3=confirmDocument cs3Label=tjenesten"))
        assertTrue(logEvent.message.contains("flexString1=22874955 flexString1Label=sakId cs5=vedtakId:9876543211 buc:P_BUC_02 sed:P6000 euxCaseId:123123"))
    }

    @Test
    fun `Test av logging av PrefillP6000 uten aktoer`() {
        auditLogger.log("confirmDocument", "", "sakId: 22874955, vedtakId: 9876543211, buc: P_BUC_02, sed: P6000, euxCaseId: 123123")

        verify(mockedAppender, atLeastOnce()).doAppend(argumentCaptor.capture())
        val logEvent = argumentCaptor.getValue()

        assertTrue(logEvent.message.contains("CEF:0|EESSI|EESSI-PENSJON|1.0|Audit:accessed|AuditLog|INFO"))
        assertTrue(logEvent.message.contains("suid=n/a cs3=confirmDocument cs3Label=tjenesten"))
        assertTrue(logEvent.message.contains("flexString1=22874955 flexString1Label=sakId cs5=vedtakId:9876543211 buc:P_BUC_02 sed:P6000 euxCaseId:123123"))
    }

    @Test
    fun `Test av logging med context uten verdi`() {
        auditLogger.log("confirmDocument", "0105094340092", "sakId: 22874955, buc: , sed:  P6000, euxCaseId: 123123")

        verify(mockedAppender, atLeastOnce()).doAppend(argumentCaptor.capture())
        val logEvent = argumentCaptor.getValue()

        assertTrue(logEvent.message.contains("CEF:0|EESSI|EESSI-PENSJON|1.0|Audit:accessed|AuditLog|INFO"))
        assertTrue(logEvent.message.contains("suid=n/a duid=0105094340092 cs3=confirmDocument cs3Label=tjenesten"))
        assertTrue(logEvent.message.contains("flexString1=22874955 flexString1Label=sakId cs5=sed:P6000 euxCaseId:123123"))
    }


    @Test
    fun `Test av logging av PrefillP2000 med context`() {
        auditLogger.log("confirmDocument", "0105094340092", "sakId: 22874955, vedtakId: 9876543211, buc: P_BUC_02, sed: P6000, euxCaseId: 123456")

        verify(mockedAppender, atLeastOnce()).doAppend(argumentCaptor.capture())
        val logEvent = argumentCaptor.getValue()

        assertTrue(logEvent.message.contains("CEF:0|EESSI|EESSI-PENSJON|1.0|Audit:accessed|AuditLog|INFO"))
        assertTrue(logEvent.message.contains("suid=n/a duid=0105094340092 cs3=confirmDocument cs3Label=tjenesten"))

        assertTrue(logEvent.message.contains("flexString1=22874955 flexString1Label=sakId cs5=vedtakId:9876543211 buc:P_BUC_02 sed:P6000 euxCaseId:123456"))

    }

    @Test
    fun `Test av logging av PrefillP2000 med context der en av verdiene er tom`() {
        auditLogger.log("confirmDocument", "0105094340092", "sakId: , vedtakId: 9876543211, buc: P_BUC_02, sed: P6000, euxCaseId: 123456")

        verify(mockedAppender, atLeastOnce()).doAppend(argumentCaptor.capture())
        val logEvent = argumentCaptor.getValue()

        assertTrue(logEvent.message.contains("CEF:0|EESSI|EESSI-PENSJON|1.0|Audit:accessed|AuditLog|INFO"))
        assertTrue(logEvent.message.contains("suid=n/a duid=0105094340092 cs3=confirmDocument cs3Label=tjenesten"))

        assertTrue(logEvent.message.contains("cs5=vedtakId:9876543211 buc:P_BUC_02 sed:P6000 euxCaseId:123456"))

    }

    @Test
    fun `Test av logging der sed er skrevet ut i loggen`() {
        auditLogger.log("confirmDocument", "0105094340092", "sed: P6000")

        verify(mockedAppender, atLeastOnce()).doAppend(argumentCaptor.capture());
        val logEvent = argumentCaptor.getValue();

        assertTrue(logEvent.message.contains("sed:P6000"))
        assertTrue(logEvent.message.contains("CEF:0|EESSI|EESSI-PENSJON|1.0|Audit:accessed|AuditLog|INFO|"))
        assertTrue(logEvent.message.contains("suid=n/a duid=0105094340092 cs3=confirmDocument cs3Label=tjenesten cs5=sed:P6000"))

    }

}



