package no.nav.eessi.pensjon.logging

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AuditLogger(private val tokenValidationContextHolder: TokenValidationContextHolder) {

    private val logger = LoggerFactory.getLogger("auditLogger")

    // Vi trenger denne no arg konstruktøren for å kunne bruke @Spy med mockito
    constructor() : this(SpringTokenValidationContextHolder())

    @Deprecated("Denne utgår", ReplaceWith("cefLog"))
    fun log(tjenesteFunctionName: String) {
        cefLog(mapOf(AuditKey.BRUKERIDENT to getSubjectfromToken(), AuditKey.TJENESTEN to tjenesteFunctionName))
    }

    @Deprecated("Denne utgår", ReplaceWith("cefLog"))
    fun log(tjenesteFunctionName: String, aktoerId: String) {
        cefLog(mapOf(AuditKey.BRUKERIDENT to getSubjectfromToken(), AuditKey.TJENESTEN to tjenesteFunctionName, AuditKey.AKTOER to aktoerId))
    }

    @Deprecated("Denne utgår", ReplaceWith("cefLog"))
    fun log(tjenesteFunctionName: String, aktoerId: String, requestContext: String) {
        cefLog(mapOf(AuditKey.BRUKERIDENT to getSubjectfromToken(), AuditKey.TJENESTEN to tjenesteFunctionName, AuditKey.AKTOER to aktoerId, AuditKey.REQUESTCONTEXT to requestContext))
    }

    @Deprecated("Denne utgår", ReplaceWith("cefLog"))
    fun logBorger(tjenesteFunctionName: String, borgerfnr: String) {
        cefLog(mapOf(AuditKey.BRUKERIDENT to getSubjectfromToken(), AuditKey.TJENESTEN to tjenesteFunctionName, AuditKey.BORGERFNR to borgerfnr))
    }

    @Deprecated("Denne utgår", ReplaceWith("cefLog"))
    fun logBorgerErr(tjenesteFunctionName: String, borgerfnr: String, errorMelding: String) {
        cefLog(mapOf(AuditKey.BRUKERIDENT to getSubjectfromToken(), AuditKey.TJENESTEN to tjenesteFunctionName, AuditKey.BORGERFNR to borgerfnr, AuditKey.ERROR to errorMelding))
    }

    @Deprecated("Denne utgår", ReplaceWith("cefLog"))
    fun logBuc(tjenesteFunctionName: String, euxData: String) {
        cefLog(mapOf(AuditKey.BRUKERIDENT to getSubjectfromToken(), AuditKey.TJENESTEN to tjenesteFunctionName, AuditKey.EUXDATA to euxData))
    }

    private fun getSubjectfromToken(): String {
        return try {
            val context = tokenValidationContextHolder.tokenValidationContext
            context.anyValidClaims.get().subject
        } catch (ex: Exception) {
            logger.warn("Brukerident ikke funnet")
            "n/a"
        }
    }

    enum class AuditKey {
        BRUKERIDENT,
        TJENESTEN,
        BORGERFNR,
        AKTOER,
        ERROR,
        EUXDATA,
        REQUESTCONTEXT
    }

    //CommonEventFormat
    //ny måte å logge på audit /cef format
    fun cefLog(values: Map<AuditKey, String>) {
        logger.info(cefHeader() + cefExtension(values))
    }

//    CEF:0|EESSI-PENSJON|AUDIT|1.0|||INFO|
//    brukerident=n/a tjenesten=addInstutionAndDocument borgerfnr= aktoer=0105094340092
//    error= euxdata= requestcontext=sakId: EESSI-PEN-123 vedtakId: 1234567 buc: P_BUC_06 sed: P6000 euxCaseId: 1234567890


//    CEF:0|EESSI|EESSI-PENSJON|1.0|audit:accessed|AuditLog|INFO|
//    suid=<Nav_ident> flexString1=addInstutionAndDocument
//    flexString1Label=tjenesten duid=fnr oldFileId=aktoerid deviceCustomString3=EESSI-PEN-123 deviceCustomString3Label=sakId
//    deviceCustomNumber1=1234567 deviceCustomNumber1Label=vedtakId deviceCustomNumber2=1234567890 deviceCustomNumber2Label=euxCaseId
//    dproc=P_BUC_06 sproc=P6000 msg=<error> flexString2=<euxdata> flexString2Label=euxdata requestcontext=


    // CEF:Version|Device Vendor|Device Product|Device Version|Device Event Class ID|Name|Severity|[Extension]
    private fun cefHeader(): String {
        return "CEF:0|EESSI|EESSI-PENSJON|Audit:accessed|AuditLog|INFO|"
    }

    private fun cefExtension(values: Map<AuditKey, String>): String {
        return String.format("suid=%s duid=%s aktoer=%s flexString1=%s flexString1Label=tjenesten flexString2=%s flexString2Label=error flexString3=%s flexString3Label=euxData %s",
                getBrukerident(values), getBorgerfnr(values), getAktoer(values), getTjenesten(values),
                getError(values), getEuxdata(values), getDelimitedContext(values))
    }

    private fun getBrukerident(values: Map<AuditKey, String>) = values.getOrDefault(AuditKey.BRUKERIDENT, "")
    private fun getTjenesten(values: Map<AuditKey, String>) = values.getOrDefault(AuditKey.TJENESTEN, "")
    private fun getBorgerfnr(values: Map<AuditKey, String>) = values.getOrDefault(AuditKey.BORGERFNR, "")
    private fun getAktoer(values: Map<AuditKey, String>) = values.getOrDefault(AuditKey.AKTOER, "")
    private fun getError(values: Map<AuditKey, String>) = values.getOrDefault(AuditKey.ERROR, "")
    private fun getEuxdata(values: Map<AuditKey, String>) = values.getOrDefault(AuditKey.EUXDATA, "")


    private fun getDelimitedContext(values: Map<AuditLogger.AuditKey, String>): String {
        val context = values.getOrDefault(AuditLogger.AuditKey.REQUESTCONTEXT, "")
        if (context.isNullOrEmpty()) return ""

        val data = context.split(" ")

        return contextExtractor(data)

    }

    private fun contextExtractor(list: List<String>) : String {
        val sb = StringBuffer()

        val contextIterator = list.iterator()
        var index = 1
        while (contextIterator.hasNext()) {
            val label = contextIterator.next().replace(":", "")
            val value = contextIterator.next()
            sb.append(String.format("deviceCustomString$index=%s deviceCustomString${index}Label=%s", value, label))
            sb.append(" ")
            index++
        }
        return sb.toString()
    }
}