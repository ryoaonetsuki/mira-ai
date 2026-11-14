package io.github.ryoaonetsuki.mira.backend

fun isVersionOlder(current: String, target: String): Boolean {
    if (target.isBlank()) return false
    if (current.isBlank()) return true

    val currentParts = current.split(".")
    val targetParts = target.split(".")
    val maxLen = maxOf(currentParts.size, targetParts.size)

    for (i in 0 until maxLen) {
        val currentSegment = currentParts.getOrElse(i) { "0" }
        val targetSegment = targetParts.getOrElse(i) { "0" }

        val currentNumeric = leadingNumeric(currentSegment)
        val targetNumeric = leadingNumeric(targetSegment)

        if (currentNumeric != targetNumeric) {
            return currentNumeric < targetNumeric
        }

        // Numeric parts equal: a pre-release suffix sorts before the same release version.
        val currentHasSuffix = currentSegment != currentNumeric.toString()
        val targetHasSuffix = targetSegment != targetNumeric.toString()

        if (currentHasSuffix && !targetHasSuffix) return true
        if (!currentHasSuffix && targetHasSuffix) return false
    }

    return false
}

private fun leadingNumeric(segment: String): Int {
    val digits = segment.takeWhile { it.isDigit() }
    return if (digits.isEmpty()) 0 else digits.toInt()
}
