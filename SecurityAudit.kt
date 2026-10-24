package dz.mesrs.webetu.automeals.core.security

import android.util.Log

object SecurityAudit {
    private const val TAG = "WebetuAutoMeals"
    private val SENSITIVE_PATTERNS = listOf(
        Regex("""(?i)password["']?\s*[:=]\s*["']?([^"'\s,]+)"""),
        Regex("""(?i)bearer\s+[a-zA-Z0-9._\-]+"""),
        Regex("""(?i)token["']?\s*[:=]\s*["']?([^"'\s,]+)"""),
        Regex("""(?i)secret["']?\s*[:=]\s*["']?([^"'\s,]+)"""),
        Regex("""(?i)cookie["']?\s*[:=]\s*["']?([^"'\s,]+)""")
    )

    fun sanitize(message: String): String {
        var clean = message
        for (pattern in SENSITIVE_PATTERNS) {
            clean = clean.replace(pattern) { matchResult ->
                val full = matchResult.value
                val prefix = full.substringBefore("=").substringBefore(":")
                if (prefix != full) "$prefix=[REDACTED]" else "[REDACTED_AUTH_HEADER]"
            }
        }
        return clean
    }

    fun i(msg: String) { Log.i(TAG, sanitize(msg)) }
    fun w(msg: String) { Log.w(TAG, sanitize(msg)) }
    fun e(msg: String, t: Throwable? = null) { Log.e(TAG, sanitize(msg), t) }
}
