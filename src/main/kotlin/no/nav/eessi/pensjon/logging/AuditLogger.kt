package no.nav.eessi.pensjon.logging

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

@Component
class AuditLogger(private val tokenValidationContextHolder: TokenValidationContextHolder){

    private val logger = LoggerFactory.getLogger("auditLogger")
    private val cef = AuditLoggerCEF()

    // Vi trenger denne no arg konstruktøren for å kunne bruke @Spy med mockito
    constructor() : this(SpringTokenValidationContextHolder())

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
            (getClaims(tokenValidationContextHolder).get("NAVident")?.toString() ?: "").also { logger.debug("NavIdenten: $it") }

        } catch (ex: Exception) {
            logger.warn("Brukerident ikke funnet")
            "n/a"
        }
    }

    //CommonEventFormat
    //ny måte å logge på audit /cef format
    fun cefLog(values: Map<AuditKey, String>) {
        logger.info(cef.getCefLog(values))
    }

    fun getClaims(tokenValidationContextHolder: TokenValidationContextHolder): JwtTokenClaims {
        val context = tokenValidationContextHolder.tokenValidationContext
        if(context.issuers.isEmpty())
            throw RuntimeException("No issuer found in context")

        val validIssuer = context.issuers.filterNot { issuer ->
            val oidcClaims = context.getClaims(issuer)
            oidcClaims.expirationTime.before(Date())
        }.map { it }


        if (validIssuer.isNotEmpty()) {
            val issuer = validIssuer.first()
            return context.getClaims(issuer)
        }
        throw RuntimeException("No valid issuer found in context")
    }

}
