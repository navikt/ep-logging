package no.nav.eessi.pensjon.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.io.ByteArrayInputStream
import java.io.InputStream

class RequestResponseLoggerInterceptor : ClientHttpRequestInterceptor {

    private val log: Logger by lazy { LoggerFactory.getLogger(RequestResponseLoggerInterceptor::class.java) }

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {

        if (log.isDebugEnabled) logRequest(request, body)

        var response: ClientHttpResponse = execution.execute(request, body)

        if (log.isDebugEnabled) {
            response = ReReadableClientHttpResponse(response)

            val responseLog = StringBuilder()
            responseLog.append("\n===========================response begin================================================")
            responseLog.append("\nStatus code    : ${response.statusCode}")
            responseLog.append("\nStatus text    : ${response.statusText}")
            responseLog.append("\nHeaders        : ${response.headers}")
            responseLog.append(trunkerBodyHvisDenErStor(response.body.readBytes()))
            responseLog.append("\n==========================response end================================================")
            log.debug(responseLog.toString())
        }
        return response
    }

    private class ReReadableClientHttpResponse(original: ClientHttpResponse) : ClientHttpResponse by original {
        val originalBody = original.body.readBytes()

        override fun getBody(): InputStream {
            return ByteArrayInputStream(originalBody)
        }
    }

    private fun logRequest(request: HttpRequest, body: ByteArray) {
        val requestLog = StringBuffer()

        requestLog.append("\n===========================request begin================================================")
        requestLog.append("\nURI            :  ${request.uri}")
        requestLog.append("\nMethod         :  ${request.method}")
        requestLog.append("\nHeaders        :  ${request.headers}")
        requestLog.append(trunkerBodyHvisDenErStor(body))
        requestLog.append("\n==========================request end================================================")
        log.debug(requestLog.toString())
    }

    private fun trunkerBodyHvisDenErStor(body: ByteArray): String {
        // Korter ned body dersom den er veldig stor ( ofte ved binærinnhold )
        return if (body.size > 5000) {
            "\nTruncated body :  ${String(body.copyOfRange(0, 5000))}"
        } else {
            "\nComplete body  :  ${String(body)}"
        }
    }
}
