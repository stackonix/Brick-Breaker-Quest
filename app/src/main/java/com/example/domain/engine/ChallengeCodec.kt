package com.example.domain.engine

import com.example.domain.model.FriendChallengeData
import kotlin.math.abs

/**
 * Encodes/decodes offline Friend Challenge codes and deep links with ZERO personal data (Section 7 & 9).
 * Also sanitizes optional nicknames to remove profanity or phone-number-like digit runs.
 */
object ChallengeCodec {
    private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // No 0/O/1/I confusion

    private val blockedWords = setOf(
        "fuck", "shit", "bitch", "asshole", "bastard", "dick", "porn", "sex",
        "madarchod", "bhenchod", "chutiya", "gandu", "harami", "kamina", "saala", "randi"
    )

    fun generateCode(nowMs: Long = System.currentTimeMillis()): String {
        val rng = DeterministicRandom(nowMs xor 0x5A3C9E1FL)
        val sb = StringBuilder("MF-")
        repeat(6) {
            sb.append(ALPHABET[rng.nextInt(ALPHABET.length)])
        }
        return sb.toString()
    }

    fun codeToSeed(code: String): Long {
        val normalized = normalizeCode(code)
        var hash = 1125899906842597L
        for (ch in normalized) {
            hash = 31L * hash + ch.code.toLong()
        }
        return abs(hash)
    }

    fun normalizeCode(raw: String): String {
        val cleaned = raw.trim().uppercase().replace(" ", "")
        return if (cleaned.startsWith("MF-")) cleaned else "MF-$cleaned"
    }

    fun buildDeepLink(code: String, score: Int, nickname: String): String {
        val safeCode = normalizeCode(code)
        val safeName = sanitizeNickname(nickname).replace(" ", "_")
        return "mindfeud://challenge?code=$safeCode&score=$score&name=$safeName"
    }

    /**
     * Accepts either a raw code ("MF-7K4P92" / "7K4P92"), a paste containing a code & score,
     * or a deep link ("mindfeud://challenge?code=MF-7K4P92&score=540&name=Aarav").
     */
    fun parseInput(rawInput: String): FriendChallengeData? {
        val text = rawInput.trim()
        if (text.isEmpty()) return null

        // Check deep link format
        if (text.contains("mindfeud://challenge", ignoreCase = true)) {
            val query = text.substringAfter("?", "")
            val params = query.split("&").mapNotNull { pair ->
                val parts = pair.split("=", limit = 2)
                if (parts.size == 2) parts[0].lowercase() to parts[1] else null
            }.toMap()
            val code = params["code"]?.let { normalizeCode(it) } ?: return null
            if (!isValidCode(code)) return null
            val score = params["score"]?.toIntOrNull()?.coerceIn(0, 1200)
            val name = sanitizeNickname((params["name"] ?: "Friend").replace("_", " "))
            return FriendChallengeData(
                code = code,
                seed = codeToSeed(code),
                challengerName = name,
                targetScore = score
            )
        }

        // Extract MF-XXXXXX pattern anywhere in pasted message
        val regex = Regex("MF-[A-Z0-9]{6}", RegexOption.IGNORE_CASE)
        val match = regex.find(text)
        val extractedCode = when {
            match != null -> match.value.uppercase()
            text.length == 6 && text.all { it.isLetterOrDigit() } -> "MF-${text.uppercase()}"
            else -> return null
        }
        if (!isValidCode(extractedCode)) return null

        val scoreRegex = Regex("score[:\\s=]+(\\d{1,4})", RegexOption.IGNORE_CASE)
        val parsedScore = scoreRegex.find(text)?.groupValues?.getOrNull(1)?.toIntOrNull()

        return FriendChallengeData(
            code = extractedCode,
            seed = codeToSeed(extractedCode),
            challengerName = "Friend",
            targetScore = parsedScore
        )
    }

    fun isValidCode(code: String): Boolean {
        val norm = normalizeCode(code)
        return norm.matches(Regex("^MF-[A-Z0-9]{6}$"))
    }

    /**
     * Section 7 safety filter: strips profanity and long digit runs (phone numbers),
     * keeps nickname short, clean, and safe for share cards.
     */
    fun sanitizeNickname(raw: String): String {
        val trimmed = raw.trim().replace(Regex("[^\\p{L}\\p{M}\\s_-]"), "")
        val collapsed = trimmed.replace(Regex("\\s+"), " ").take(16).trim()
        if (collapsed.isEmpty()) return "Player"
        val lower = collapsed.lowercase()
        if (blockedWords.any { lower.contains(it) }) {
            return "Player"
        }
        return collapsed
    }
}
