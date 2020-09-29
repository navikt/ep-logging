package no.nav.eessi.pensjon.logging.cef

import no.nav.eessi.pensjon.logging.AuditLogger
import org.springframework.stereotype.Component

@Component
class CommonEventFormat {

    fun getCefLog(values: Map<AuditLogger.AuditKey, String>) :String {
        return cefHeader()+cefExtension(values)
    }

    // CEF:Version|Device Vendor|Device Product|Device Version|Device Event Class ID|Name|Severity|[Extension]
    fun cefHeader(): String {
        return "CEF:0|EESSI|EESSI-PENSJON|Audit:accessed|AuditLog|INFO|"
    }

    fun cefExtension(values: Map<AuditLogger.AuditKey, String>): String {
        return String.format("end=%s %s%s%scs3=%s cs3Label=tjenesten %s ",
                getTimeStamp(), getBrukerident(values), getBorgerfnr(values), getAktoer(values), getTjenesten(values),
                getDelimitedContext(values))
    }

    private fun getTimeStamp() = System.currentTimeMillis().toString()
    private fun getBrukerident(values: Map<AuditLogger.AuditKey, String>) = filterOutUnusedField("suid=", values.getOrDefault(AuditLogger.AuditKey.BRUKERIDENT, "")+ " ")
    private fun getBorgerfnr(values: Map<AuditLogger.AuditKey, String>) = filterOutUnusedField("duid=", values.getOrDefault(AuditLogger.AuditKey.BORGERFNR, "") + " ")
    private fun getAktoer(values: Map<AuditLogger.AuditKey, String>) = filterOutUnusedField("aktoer=", values.getOrDefault(AuditLogger.AuditKey.AKTOER,  "") + " ")
    private fun getTjenesten(values: Map<AuditLogger.AuditKey, String>) = values.getOrDefault(AuditLogger.AuditKey.TJENESTEN, "")

    private fun filterOutUnusedField(field: String, value: String): String {
        return if (value.isBlank()) {
            ""
        } else {
            field+value
        }
    }

    private fun getDelimitedContext(values: Map<AuditLogger.AuditKey, String>): String {
        val context = values.getOrDefault(AuditLogger.AuditKey.REQUESTCONTEXT, "")
        val euxCaseId = values.getOrDefault(AuditLogger.AuditKey.EUXCASEID, "")

        if (euxCaseId.isNotBlank() && context.isNullOrEmpty()) {
            return "cs5=euxCaseId:$euxCaseId"
        }
        if (context.isNullOrEmpty()) return ""
        val data = context.split(" ")
        val map = contextExtractor(data)

        return getDelimitedContextText(map)

    }

    private fun contextExtractor(list: List<String>): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val contextIterator = list.iterator()
        while (contextIterator.hasNext()) {
            val label = contextIterator.next().replace(":", "")
            val value = contextIterator.next()
            map.put(label, value)
        }
        return map
    }

    private fun getDelimitedContextText(map: Map<String, String>): String {
        val sb = StringBuffer()
        val sakResult = map.filter { it.key == "sakId" }
                .map { "flexString1=${it.value} flexString1Label=sakId " }
        if (sakResult.isNotEmpty()) {
            sb.append(sakResult.first())
        }
        val validkey = listOf("vedtakId", "buc", "sed", "euxCaseId")
        val extraResult = map.filterKeys { validkey.contains(it) }
        if (extraResult.isNotEmpty()) {
            val result = extraResult.map { "${it.key}:${it.value}" }
            if (result.isNotEmpty()) {
                sb.append("cs5=").append(result.joinToString(" "))
            }
        }
        return sb.toString()
    }

}


