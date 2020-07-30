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

    fun log(tjenesteFunctionName: String) {
        logger.info("brukerident: ${getSubjectfromToken()} tjenesten: $tjenesteFunctionName")
    }

    fun log(tjenesteFunctionName: String, aktoerId: String) {
        logger.info("brukerident: ${getSubjectfromToken()} tjenesten: $tjenesteFunctionName aktoerId: $aktoerId")
    }

    fun log(tjenesteFunctionName: String, aktoerId: String,  requestContext: String) {
        logger.info("brukerident: ${getSubjectfromToken()} tjenesten: $tjenesteFunctionName aktoerId: $aktoerId $requestContext")
    }

    fun logBorger(tjenesteFunctionName: String, borgerfnr: String) {
        logger.info("brukerident: ${getSubjectfromToken()} tjenesten: $tjenesteFunctionName fnr: $borgerfnr")
    }

    fun logBorgerErr(tjenesteFunctionName: String, borgerfnr: String, errorMelding: String) {
        logger.info("brukerident: ${getSubjectfromToken()} tjenesten: $tjenesteFunctionName fnr: $borgerfnr feilmelding: $errorMelding")
    }

    fun logBuc(tjenesteFunctionName: String, euxData: String) {
        logger.info("brukerident: ${getSubjectfromToken()} tjenesten: $tjenesteFunctionName $euxData")
    }

    private fun getSubjectfromToken() : String {
        return try {
            val context = tokenValidationContextHolder.tokenValidationContext
            return context.anyValidClaims.get().subject
        } catch (ex: Exception) {
            logger.error("Brukerident ikke funnet")
            "n/a"
        }
    }
}