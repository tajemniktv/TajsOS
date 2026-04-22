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

    /**
     * Represents an IPv4 address.
     */
    data class Ipv4(val address: Long) : IpAddress() {
        override fun isLoopback(): Boolean = (address ushr 24) == 127L
        override fun isLinkLocal(): Boolean = (address ushr 16) == 0xA9FEL // 169.254.x.x
        override fun isSiteLocal(): Boolean {
            val first = address ushr 24
            val second = (address ushr 16) and 0xFF
            return first == 10L || (first == 172L && second in 16..31) || (first == 192L && second == 168L)
        }
    }

    /**
     * Represents an IPv6 address.
     */
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
    val cleanHost = host.trim().removePrefix("[").removeSuffix("]").trim()
    return parseIpv4(cleanHost) ?: parseIpv6(cleanHost)
}

private fun parseIpv4(host: String): IpAddress.Ipv4? {
    val parts = host.split(".")
    if (parts.size != 4) return null
    var address = 0L
    for (i in 0..3) {
        val part = parts[i]
        if (part.isEmpty()) return null
        // Disallow leading spaces or any other non-digit character implicitly
        if (part.any { it !in '0'..'9' }) return null
        // Disallow leading zeros unless the part is exactly "0"
        if (part.length > 1 && part.startsWith("0")) return null
        val byteVal = part.toLongOrNull() ?: return null
        if (byteVal !in 0..255) return null
        address = (address shl 8) or byteVal
    }
    return IpAddress.Ipv4(address)
}

private fun validateIpv6Host(host: String): Boolean {
    if (!host.contains(":")) return false
    if (host.startsWith(":") && !host.startsWith("::")) return false
    if (host.endsWith(":") && !host.endsWith("::")) return false

    // An IPv6 address can only have at most one "::" substitution.
    // If the first and last occurrences of "::" are not the same, it's invalid.
    if (host.indexOf("::") != host.lastIndexOf("::")) {
        return false
    }
    return true
}

private fun parseIpv6(host: String): IpAddress.Ipv6? {
    if (!validateIpv6Host(host)) return null

    val rawParts = host.split(":")

    val values = LongArray(8) { 0L }
    val doubleColonIdx = rawParts.indexOf("")

    val success = if (doubleColonIdx != -1) {
        parseIpv6Abbreviated(rawParts, values, doubleColonIdx)
    } else {
        parseIpv6Full(rawParts, values)
    }

    if (!success) return null
    return IpAddress.Ipv6(packIpv6LongArray(values))
}

private fun parseIpv6Abbreviated(rawParts: List<String>, values: LongArray, doubleColonIdx: Int): Boolean {
    val leftParsed = populateIpv6Values(rawParts, 0 until doubleColonIdx, values, 0, 1)
    if (leftParsed == -1) return false

    val rightParsed = populateIpv6Values(rawParts, rawParts.lastIndex downTo doubleColonIdx + 1, values, 7, -1)
    if (rightParsed == -1) return false

    return leftParsed <= (7 - rightParsed)
}

private fun parseIpv6Segment(part: String): Long? {
    if (part.length > 4) return null
    if (part.startsWith("-") || part.startsWith("+")) return null
    val v = part.toLongOrNull(16) ?: return null
    if (v !in 0..0xFFFF) return null
    return v
}

private fun populateIpv6Values(
    rawParts: List<String>,
    indices: IntProgression,
    values: LongArray,
    startOutputIndex: Int,
    direction: Int
): Int {
    var outputIdx = startOutputIndex
    var count = 0
    for (i in indices) {
        val part = rawParts[i]
        if (part.isEmpty()) {
            // Empty parts should only exist at the boundaries (e.g. "::1" -> "", "", "1").
            // If we encounter one here during population, and it's not handled by the boundary cases,
            // we skip it, but we already validated that there are no extra "::" in parseIpv6.
            continue
        }
        val v = parseIpv6Segment(part) ?: return -1
        if (outputIdx < 0 || outputIdx >= values.size) return -1
        values[outputIdx] = v
        outputIdx += direction
        count++
    }
    return count
}

private fun parseIpv6Full(rawParts: List<String>, values: LongArray): Boolean {
    if (rawParts.size != 8) return false
    for (i in 0..7) {
        val v = parseIpv6Segment(rawParts[i]) ?: return false
        values[i] = v
    }
    return true
}

private fun packIpv6LongArray(values: LongArray): LongArray {
    val finalWords = LongArray(4)
    finalWords[0] = (values[0] shl 16) or values[1]
    finalWords[1] = (values[2] shl 16) or values[3]
    finalWords[2] = (values[4] shl 16) or values[5]
    finalWords[3] = (values[6] shl 16) or values[7]
    return finalWords
}
