package no.nav.eessi.pensjon.logging

class AuditLoggerCEF {

    fun getCefLog(values: Map<AuditKey, String>) :String {
        return cefHeader()+cefExtension(values)
    }

    // CEF:Version|Device Vendor|Device Product|Device Version|Device Event Class ID|Name|Severity|[Extension]
    fun cefHeader(): String {
        return "CEF:0|EESSI|EESSI-PENSJON|1.0|Audit:accessed|AuditLog|INFO|"
    }

    fun cefExtension(values: Map<AuditKey, String>): String {
        return String.format("end=%s %s%scs3=%s cs3Label=tjenesten %s ",
                getTimeStamp(), getBrukerident(values), getAktoer(values), getTjenesten(values),
                getDelimitedContext(values))
    }

    private fun getTimeStamp() = System.currentTimeMillis().toString()
    private fun getBrukerident(values: Map<AuditKey, String>) = filterOutUnusedField("suid=", values.getOrDefault(AuditKey.BRUKERIDENT, "")+ " ")
    private fun getAktoer(values: Map<AuditKey, String>) = filterOutUnusedField("duid=", values.getOrDefault(AuditKey.AKTOER,  "") + " ")
    private fun getTjenesten(values: Map<AuditKey, String>) = values.getOrDefault(AuditKey.TJENESTEN, "")

    private fun filterOutUnusedField(field: String, value: String) = if(value.isBlank()) "" else field+value

    private fun getDelimitedContext(values: Map<AuditKey, String>): String {
        val context = values.getOrDefault(AuditKey.REQUESTCONTEXT, "")
        val euxCaseId = values.getOrDefault(AuditKey.EUXCASEID, "")

        if (euxCaseId.isNotBlank() && context.isEmpty()) {
            return "cs5=euxCaseId:$euxCaseId"
        }
        if (context.isEmpty()) return ""
        val datatmp = context.replace(": ", ":")
        val map = contextExtractor(datatmp.split(" "))

        return getDelimitedContextText(map)

    }

    private fun contextExtractor(list: List<String>): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (str in list) {
            val keyval = str.split(":")
            val label = keyval[0]
            val value = keyval[1]
            if (value.isNotEmpty())
                map[label] = value
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


