package no.nav.eessi.pensjon.logging

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

}