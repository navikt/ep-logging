package no.nav.eessi.pensjon.logging

import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class RequestResponseLoggerFilter(private val overrideLogger: Logger? = null) : Filter {

    private val logger: Logger by lazy { overrideLogger ?: LoggerFactory.getLogger(RequestResponseLoggerFilter::class.java) }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        try {
            val httpServletRequest = request as HttpServletRequest
            val requestURI = httpServletRequest.requestURI
            if (!requestURI.startsWith("/internal/") && !requestURI.startsWith("/actuator/")  ) {
                logger.debug(httpServletRequest.requestURI)
            }
        } catch (e: Exception) {
            logger.warn("Logging failed, continuing.", e)
        }
        chain.doFilter(request, response)
    }

}
