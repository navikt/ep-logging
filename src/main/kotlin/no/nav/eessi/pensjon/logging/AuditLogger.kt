package no.nav.eessi.pensjon.logging

import no.nav.eessi.pensjon.logging.cef.CommonEventFormat
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AuditLogger(private val tokenValidationContextHolder: TokenValidationContextHolder,
        private val cef: CommonEventFormat) {

    private val logger = LoggerFactory.getLogger("auditLogger")

    // Vi trenger denne no arg konstruktøren for å kunne bruke @Spy med mockito
    constructor() : this(SpringTokenValidationContextHolder(), CommonEventFormat())

    fun log(tjenesteFunctionName: String) {
        cefLog(mapOf(AuditKey.BRUKERIDENT to getSubjectfromToken(), AuditKey.TJENESTEN to tjenesteFunctionName))
    }

    fun log(tjenesteFunctionName: String, aktoerId: String) {
        cefLog(mapOf(AuditKey.BRUKERIDENT to getSubjectfromToken(), AuditKey.TJENESTEN to tjenesteFunctionName, AuditKey.AKTOER to aktoerId))
    }

    fun log(tjenesteFunctionName: String, aktoerId: String, requestContext: String) {
        cefLog(mapOf(AuditKey.BRUKERIDENT to getSubjectfromToken(), AuditKey.TJENESTEN to tjenesteFunctionName, AuditKey.AKTOER to aktoerId, AuditKey.REQUESTCONTEXT to requestContext))
    }

    fun logBorger(tjenesteFunctionName: String, borgerfnr: String) {
        cefLog(mapOf(AuditKey.BRUKERIDENT to getSubjectfromToken(), AuditKey.TJENESTEN to tjenesteFunctionName, AuditKey.BORGERFNR to borgerfnr))
    }

    fun logBuc(tjenesteFunctionName: String, euxData: String) {
        cefLog(mapOf(AuditKey.BRUKERIDENT to getSubjectfromToken(), AuditKey.TJENESTEN to tjenesteFunctionName, AuditKey.EUXCASEID to euxData))
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
        EUXCASEID,
        REQUESTCONTEXT
    }

    //CommonEventFormat
    //ny måte å logge på audit /cef format
    fun cefLog(values: Map<AuditKey, String>) {
        logger.info(cef.getCefLog(values))
    }


}
