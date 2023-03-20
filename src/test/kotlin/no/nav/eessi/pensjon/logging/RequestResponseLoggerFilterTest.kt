package no.nav.eessi.pensjon.logging

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.Marker
import org.slf4j.event.Level
import org.slf4j.helpers.AbstractLogger
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse


class RequestResponseLoggerFilterTest {

    private val mockFilterChain = MockFilterChain()
    private val mockRequest = MockHttpServletRequest()
    private val mockResponse = MockHttpServletResponse()

    @Test
    fun `Filter kaller videre ned i kjeden`() {
        val filter = RequestResponseLoggerFilter()
        filter.doFilter(mockRequest, mockResponse, mockFilterChain)

        assertEquals(mockRequest, mockFilterChain.request)
        assertEquals(mockResponse, mockFilterChain.response)
    }

    @Test
    fun `Hvis debug er enablet logger vi requesten`() {
        val spyLogger = SpyLogger()
        spyLogger.debugEnabled = true

        mockRequest.requestURI = "/foo?bar=baz"

        val filter = RequestResponseLoggerFilter(spyLogger)
        filter.doFilter(mockRequest, mockResponse, mockFilterChain)

        val actual = spyLogger.observations.first()
        assertEquals(Observation(Level.DEBUG,"/foo?bar=baz"), actual)
    }

    @Test
    fun `Hvis debug ikke er enablet logger vi ikke requesten`() {
        val spyLogger = SpyLogger()
        spyLogger.debugEnabled = false

        mockRequest.requestURI = "/foo?bar=baz"

        val filter = RequestResponseLoggerFilter(spyLogger)
        filter.doFilter(mockRequest, mockResponse, mockFilterChain)

        assertTrue(spyLogger.observations.isEmpty())
    }

    @Test
    fun `Exception i logging skal ikke stoppe filter-kjeden`() {
        val exception = Exception("Shit in fan")
        val explodingLogger = object : SpyLogger() {
            override fun debug(msg: String) {
                throw exception
            }
        }

        val filter = RequestResponseLoggerFilter(explodingLogger)
        filter.doFilter(mockRequest, mockResponse, mockFilterChain)

        assertEquals(mockRequest, mockFilterChain.request)
        assertEquals(mockResponse, mockFilterChain.response)

        val actual = explodingLogger.observations.first()
        assertEquals(Observation(Level.WARN,"Logging failed, continuing.", exception), actual)
    }

}

data class Observation(val level: Level?, val msg: String?, val throwable: Throwable? = null)

open class SpyLogger : AbstractLogger() {
    var observations: List<Observation> = emptyList()
    var debugEnabled: Boolean = false

    override fun isDebugEnabled(): Boolean = debugEnabled
    override fun isWarnEnabled(): Boolean = true

    //Ignorerer disse metodene
    override fun isTraceEnabled(): Boolean = TODO("Not yet implemented")
    override fun isTraceEnabled(marker: Marker?): Boolean = TODO("Not yet implemented")
    override fun isDebugEnabled(marker: Marker?): Boolean = TODO("Not yet implemented")
    override fun isInfoEnabled(): Boolean = TODO("Not yet implemented")
    override fun isInfoEnabled(marker: Marker?): Boolean = TODO("Not yet implemented")
    override fun isWarnEnabled(marker: Marker?): Boolean = TODO("Not yet implemented")
    override fun isErrorEnabled(): Boolean = TODO("Not yet implemented")
    override fun isErrorEnabled(marker: Marker?): Boolean = TODO("Not yet implemented")
    override fun getFullyQualifiedCallerName(): String = TODO("Not yet implemented")
    override fun handleNormalizedLoggingCall(
        level: Level?,
        marker: Marker?,
        messagePattern: String?,
        arguments: Array<out Any>?,
        throwable: Throwable?
    ) {
        observations += Observation(level, messagePattern, throwable)
    }

}
