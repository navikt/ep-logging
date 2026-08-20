package no.nav.eessi.pensjon.logging

import org.slf4j.LoggerFactory

class AuditLoggerCEF {

    private val logger = LoggerFactory.getLogger(AuditLoggerCEF::class.java)

    fun getCefLog(values: Map<AuditKey, String>): String {
        return cefHeader() + cefExtension(values)
    }

    // CEF:Version|Device Vendor|Device Product|Device Version|Device Event Class ID|Name|Severity|[Extension]
    fun cefHeader(): String {
        return "CEF:0|EESSI|EESSI-PENSJON|1.0|Audit:accessed|AuditLog|INFO|"
    }

    fun cefExtension(values: Map<AuditKey, String>): String {
        return String.format(
            "end=%s %s%scs3=Tjeneste:%s %scs5=RequestContext: %s",
            getTimeStamp(), getBrukerident(values), getAktoer(values), getTjenesten(values), getBucType(values),
            getDelimitedContext(values)
        )
    }

    private fun getTimeStamp() = System.currentTimeMillis().toString()
    private fun getBrukerident(values: Map<AuditKey, String>) =
        filterOutUnusedField("suid=", values.getOrDefault(AuditKey.BRUKERIDENT, "") + " ")

    private fun getAktoer(values: Map<AuditKey, String>) =
        filterOutUnusedField("duid=", values.getOrDefault(AuditKey.AKTOER, "") + " ")

    private fun getBucType(values: Map<AuditKey, String>) =
        filterOutUnusedField("buctype=", values.getOrDefault(AuditKey.BUCTYPE, "") + " ")

    private fun getTjenesten(values: Map<AuditKey, String>) = values.getOrDefault(AuditKey.TJENESTEN, "")

    private fun filterOutUnusedField(field: String, value: String) = if (value.isBlank()) "" else field + value

    private fun getDelimitedContext(values: Map<AuditKey, String>): String {
        val context = values.getOrDefault(AuditKey.REQUESTCONTEXT, "")
        val euxCaseId = values.getOrDefault(AuditKey.EUXCASEID, "")

        if (euxCaseId.isNotBlank() && context.isEmpty()) {
            return "cs5=euxCaseId:$euxCaseId"
        }
        if (context.isEmpty()) return ""
        val datatmp = context.replace(" ", "")
        val map = contextExtractor(datatmp.split(","))

        return getDelimitedContextText(map)

    }

    private fun contextExtractor(list: List<String>): Map<String, String> {
        return list.map { it.split(":") }
            .filterNot { keyvalue -> keyvalue.last().isEmpty() }
            .map { keyvalue -> keyvalue.first() to keyvalue.last() }.toMap()
    }

    private fun getDelimitedContextText(map: Map<String, String>): String {
        val sb = StringBuffer()
        val validkey = listOf("vedtakId", "buc", "sed", "euxCaseId", "documentId", "bucType", "journalpostId", "sakId")
        val extraResult = map.filterKeys { validkey.contains(it) }
        try {
            if (extraResult.isNotEmpty() && extraResult.values.isNotEmpty()) {
                val result = extraResult.map { "${it.key}:${it.value}" }
                if (result.isNotEmpty()) {
                    sb.append(result.joinToString(" "))
                }
            }
        } catch (ex: Exception) {
            logger.warn(ex.message, ex)
        }
        return sb.toString()
    }

}


