package no.nav.eessi.pensjon.logging

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AuditLogger(private val tokenValidationContextHolder: TokenValidationContextHolder) {

    private val logger =  LoggerFactory.getLogger("auditLogger")

    // Vi trenger denne no arg konstruktøren for å kunne bruke @Spy med mockito
    constructor() : this(SpringTokenValidationContextHolder())

    fun log(tjenesteFunctionName: String) {
        cefLog(mapOf(AuditKey.BRUKERIDENT to getSubjectfromToken(), AuditKey.TJENESTEN to tjenesteFunctionName))
    }

    fun log(tjenesteFunctionName: String, aktoerId: String) {
        cefLog(mapOf(AuditKey.BRUKERIDENT to getSubjectfromToken(), AuditKey.TJENESTEN to tjenesteFunctionName, AuditKey.AKTOER to aktoerId))
    }

    fun log(tjenesteFunctionName: String, aktoerId: String,  requestContext: String) {
        cefLog(mapOf(AuditKey.BRUKERIDENT to getSubjectfromToken(), AuditKey.TJENESTEN to tjenesteFunctionName, AuditKey.AKTOER to aktoerId, AuditKey.REQUESTCONTEXT to requestContext))
    }

    fun logBorger(tjenesteFunctionName: String, borgerfnr: String) {
        cefLog(mapOf(AuditKey.BRUKERIDENT to getSubjectfromToken(), AuditKey.TJENESTEN to tjenesteFunctionName, AuditKey.BORGERFNR to borgerfnr))
    }

    fun logBorgerErr(tjenesteFunctionName: String, borgerfnr: String, errorMelding: String) {
        cefLog(mapOf(AuditKey.BRUKERIDENT to getSubjectfromToken(), AuditKey.TJENESTEN to tjenesteFunctionName, AuditKey.BORGERFNR to borgerfnr, AuditKey.ERROR to errorMelding))
    }

    fun logBuc(tjenesteFunctionName: String, euxData: String) {
        cefLog(mapOf(AuditKey.BRUKERIDENT to getSubjectfromToken(), AuditKey.TJENESTEN to tjenesteFunctionName, AuditKey.EUXDATA to euxData))
    }

    private fun getSubjectfromToken() : String {
        return try {
            val context = tokenValidationContextHolder.tokenValidationContext
            return context.anyValidClaims.get().subject
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
        logger.info( cefHeader() + cefExtension(values) )
    }

    fun getCefLog(values: Map<AuditKey, String>): String {
        return cefHeader() + cefExtension(values)
    }

    // CEF:Version|Device Vendor|Device Product|Device Version|Device Event Class ID|Name|Severity|[Extension]
    private fun cefHeader(): String {
        return "CEF:0|EESSI-PENSJON|AUDIT|1.0|||INFO|"
    }

    private fun cefExtension(values: Map<AuditKey, String>): String {
        return String.format("brukerident=%s tjenesten=%s borgerfnr=%s aktoer=%s error=%s euxdata=%s requestcontext=%s",
                getBrukerident(values), getTjenesten(values), getBorgerfnr(values), getAktoer(values),
                getError(values), getEuxdata(values), getContext(values) )
    }

    private fun getBrukerident(values: Map<AuditKey, String>) = values.getOrDefault(AuditKey.BRUKERIDENT, "")
    private fun getTjenesten(values: Map<AuditKey, String>) = values.getOrDefault(AuditKey.TJENESTEN, "")
    private fun getBorgerfnr(values: Map<AuditKey, String>) = values.getOrDefault(AuditKey.BORGERFNR, "")
    private fun getAktoer(values: Map<AuditKey, String>) = values.getOrDefault(AuditKey.AKTOER, "")
    private fun getError(values: Map<AuditKey, String>) = values.getOrDefault(AuditKey.ERROR, "")
    private fun getEuxdata(values: Map<AuditKey, String>) = values.getOrDefault(AuditKey.EUXDATA, "")
    private fun getContext(values: Map<AuditKey, String>) = values.getOrDefault(AuditKey.REQUESTCONTEXT, "")


}