package com.tajemniktv.tajsos.calendar

/**
 * A lightweight pure-Kotlin IP address parser to safely validate external endpoints against SSRF
 * without relying on platform-specific DNS resolution (which varies across KMP targets) or naive string prefixing.
 */
internal sealed class IpAddress {
    abstract fun isLoopback(): Boolean
    abstract fun isLinkLocal(): Boolean
    abstract fun isSiteLocal(): Boolean

    fun isPrivateOrLocal(): Boolean = isLoopback() || isLinkLocal() || isSiteLocal()

    data class Ipv4(val address: Long) : IpAddress() {
        override fun isLoopback(): Boolean = (address ushr 24) == 127L
        override fun isLinkLocal(): Boolean = (address ushr 16) == 0xA9FEL // 169.254.x.x
        override fun isSiteLocal(): Boolean {
            val first = address ushr 24
            val second = (address ushr 16) and 0xFF
            return first == 10L || (first == 172L && second in 16..31) || (first == 192L && second == 168L)
        }
    }

    data class Ipv6(val words: LongArray) : IpAddress() {
        override fun isLoopback(): Boolean = words[0] == 0L && words[1] == 0L && words[2] == 0L && words[3] == 1L

        override fun isLinkLocal(): Boolean = (words[0] ushr 16) == 0xFE80L // fe80::/10

        override fun isSiteLocal(): Boolean {
            val prefix7 = (words[0] ushr 25)
            return prefix7 == 0x7EL || prefix7 == 0x7FL // fc00::/7 => fc00 to fdff
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as Ipv6
            return words.contentEquals(other.words)
        }

        override fun hashCode(): Int = words.contentHashCode()
    }
}

/**
 * Attempts to parse a host string into an [IpAddress].
 * Returns null if the string is not a valid IPv4 or IPv6 literal.
 */
internal fun parseIpAddress(host: String): IpAddress? {
    val cleanHost = host.removePrefix("[").removeSuffix("]").trim()
    return parseIpv4(cleanHost) ?: parseIpv6(cleanHost)
}

private fun parseIpv4(host: String): IpAddress.Ipv4? {
    val parts = host.split(".")
    if (parts.size != 4) return null
    var address = 0L
    for (i in 0..3) {
        val byteVal = parts[i].toLongOrNull() ?: return null
        if (byteVal !in 0..255) return null
        address = (address shl 8) or byteVal
    }
    return IpAddress.Ipv4(address)
}

private fun parseIpv6(host: String): IpAddress.Ipv6? {
    if (!host.contains(":")) return null
    val rawParts = host.split(":")

    val values = LongArray(8) { 0L }
    var leftIdx = 0

    val doubleColonIdx = rawParts.indexOf("")
    if (doubleColonIdx != -1) {
        // Left side of ::
        for (i in 0 until doubleColonIdx) {
            val p = rawParts[i]
            if (p.isEmpty()) continue
            val v = p.toLongOrNull(16) ?: return null
            if (v !in 0..0xFFFF) return null
            values[leftIdx++] = v
        }

        // Right side of ::
        var rightIdx = 7
        for (i in rawParts.lastIndex downTo doubleColonIdx + 1) {
            val p = rawParts[i]
            if (p.isEmpty()) continue
            val v = p.toLongOrNull(16) ?: return null
            if (v !in 0..0xFFFF) return null
            values[rightIdx--] = v
        }
    } else {
        if (rawParts.size != 8) return null
        for (i in 0..7) {
            val v = rawParts[i].toLongOrNull(16) ?: return null
            if (v !in 0..0xFFFF) return null
            values[i] = v
        }
    }

    val finalWords = LongArray(4)
    finalWords[0] = (values[0] shl 16) or values[1]
    finalWords[1] = (values[2] shl 16) or values[3]
    finalWords[2] = (values[4] shl 16) or values[5]
    finalWords[3] = (values[6] shl 16) or values[7]
    return IpAddress.Ipv6(finalWords)
}
