package no.nav.eessi.pensjon.logging

import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.security.oidc.context.TokenContext
import no.nav.security.spring.oidc.SpringOIDCRequestContextHolder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AuditLogger(private val oidcRequestContextHolder: OIDCRequestContextHolder) {
    private val logger = LoggerFactory.getLogger("auditLogger")

    // Vi trenger denne no arg konstruktøren for å kunne bruke @Spy med mockito
    constructor() : this(SpringOIDCRequestContextHolder())

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
            val context = oidcRequestContextHolder.oidcValidationContext
            val tokenContext = getTokenContext("isso")
            val issuer = tokenContext.issuer
            context.getClaims(issuer).subject
        } catch (ex: Exception) {
            logger.error("Brukerident ikke funnet")
            "n/a"
        }
    }

    private fun getTokenContext(tokenKey: String): TokenContext {
        val context = oidcRequestContextHolder.oidcValidationContext
        if (context.issuers.isEmpty()) throw RuntimeException("No issuer found in context")
        val tokenkeys = context.issuers
        if (tokenkeys.contains(tokenKey)) {
            return context.getToken(tokenKey)
        }
        throw RuntimeException("No issuer found in context")
    }



}