package no.nav.eessi.pensjon.logging

import no.nav.eessi.pensjon.logging.RequestResponseLoggerFilter.OverrideLogger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
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
    fun `Hvis debug er enablet logger vi requesten`(){
        val fakeLogger = FakeLoggerImpl()
        val filter = RequestResponseLoggerFilter(fakeLogger)

        mockRequest.requestURI = "/foo?bar=baz"
        filter.doFilter(mockRequest,mockResponse, mockFilterChain)

        val actual = fakeLogger.logLine
        assertEquals("/foo?bar=baz", actual)
    }

    @Test
    fun `Exception i logging skal ikke stoppe filter-kjeden`() {
        val explodingLogger = object : OverrideLogger {
            override fun debug(msg: String) {
                throw Exception("Shit in fan")
            }
        }
        val filter = RequestResponseLoggerFilter(explodingLogger)

        filter.doFilter(mockRequest, mockResponse, mockFilterChain)

        assertEquals(mockRequest, mockFilterChain.request)
        assertEquals(mockResponse, mockFilterChain.response)
    }

    class FakeLoggerImpl : OverrideLogger {
        var logLine: String? = null
        override fun debug(msg: String) {
            logLine = msg
        }
    }
}