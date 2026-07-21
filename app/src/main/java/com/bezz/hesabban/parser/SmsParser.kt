package com.bezz.hesabban.parser

import java.security.MessageDigest

/**
 * Result of parsing one SMS. Category/account attribution is resolved by the
 * caller (SmsReceiver/SmsImporter), which has DB access; this object only
 * carries what could be extracted from the raw text.
 */
data class ParsedSms(
    val type: String,           // "INCOME" or "EXPENSE"
    val amount: Long,           // Toman
    val balance: Long?,         // Toman, null if not present in the SMS
    val accountLast4: String?,
    val rawBody: String,
    val normalizedBody: String,
    val hash: String            // sha1(sender + normalizedBody), dedupe key
)

/**
 * Implements SMS_PARSING.md: normalize -> keyword-gate -> extract amount/balance
 * -> extract account last-4 -> hash for dedupe. Sender allow-list filtering and
 * OTP/ad rejection both happen here too, since they're cheap and self-contained.
 *
 * This is a heuristic parser [محتمل] — real bank SMS formats vary. The generic
 * rule below is deliberately last-resort; extend the per-bank notes in
 * SMS_PARSING.md if a real message fails to parse.
 */
object SmsParser {

    private val withdrawKeywords = listOf(
        "برداشت", "خرید", "خريد", "پرداخت", "کسر", "انتقال از", "چک", "کارمزد"
    )
    private val depositKeywords = listOf(
        "واریز", "واريز", "انتقال به حساب", "افزایش", "حقوق"
    )
    // Never store OTP / verification-code messages, even from an allowed sender.
    private val ignoreKeywords = listOf("رمز", "کد تایید", "otp", "کد فعال")

    private val amountRegex = Regex(
        """(?:برداشت|واریز|خرید|خريد|پرداخت|کسر|انتقال)[^\d]{0,20}(\d{3,15})\s*(ریال|ريال|تومان|تومن)?"""
    )
    private val balanceRegex = Regex(
        """(?:مانده|موجودی)[^\d]{0,15}(\d{3,15})\s*(ریال|ريال|تومان|تومن)?"""
    )
    private val last4Regex = Regex("""(?:کارت|حساب|سپرده)[^\d]*(\d{4})""")
    private val genericNumberRegex = Regex("""\d{4,15}""")

    val categoryKeywords: Map<String, List<String>> = linkedMapOf(
        "خوراک" to listOf(
            "کافه", "رستوران", "فست", "نان", "هایپر", "سوپر", "میوه",
            "اسنپ فود", "snappfood"
        ),
        "حمل‌ونقل" to listOf(
            "اسنپ", "تپسی", "بنزین", "مترو", "اتوبوس", "عوارض", "پارکینگ"
        ),
        "قبض‌ها" to listOf(
            "قبض", "برق", "آب", "گاز", "تلفن", "شارژ", "ایرانسل", "همراه اول", "اینترنت"
        ),
        "خرید" to listOf(
            "دیجی", "فروشگاه", "پوشاک", "خرید اینترنتی"
        )
        // "درآمد" and "سایر" are not keyword-matched — see categorize().
    )

    /** Step 0: normalize Persian/Arabic digits, separators, and letter variants. */
    fun normalize(body: String): String {
        val persianDigits = "۰۱۲۳۴۵۶۷۸۹"
        val arabicDigits = "٠١٢٣٤٥٦٧٨٩"
        val sb = StringBuilder(body.length)
        for (c in body) {
            val pi = persianDigits.indexOf(c)
            val ai = arabicDigits.indexOf(c)
            when {
                pi >= 0 -> sb.append(('0' + pi))
                ai >= 0 -> sb.append(('0' + ai))
                c == 'ي' -> sb.append('ی')
                c == 'ك' -> sb.append('ک')
                else -> sb.append(c)
            }
        }
        var out = sb.toString()
        out = out.replace(",", "").replace("٬", "")
        out = out.replace(Regex(" {2,}"), " ")
        return out
    }

    private fun sha1(input: String): String {
        val digest = MessageDigest.getInstance("SHA-1").digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Parses a single SMS. Returns null when it's not a transaction message at
     * all (OTP, ad, balance-only ping with no amount, etc) — those are ignored,
     * never stored. Caller is responsible for the sender allow-list check.
     */
    fun parse(sender: String?, rawBody: String): ParsedSms? {
        val normalized = normalize(rawBody)

        if (ignoreKeywords.any { normalized.contains(it, ignoreCase = true) }) return null

        val isWithdraw = withdrawKeywords.any { normalized.contains(it) }
        val isDeposit = depositKeywords.any { normalized.contains(it) }
        if (!isWithdraw && !isDeposit) return null // not a transaction SMS

        // If both keyword types appear, the first occurrence in the text wins.
        val type = if (isWithdraw && isDeposit) {
            val wIdx = withdrawKeywords.mapNotNull { kw ->
                val i = normalized.indexOf(kw); if (i >= 0) i else null
            }.minOrNull() ?: Int.MAX_VALUE
            val dIdx = depositKeywords.mapNotNull { kw ->
                val i = normalized.indexOf(kw); if (i >= 0) i else null
            }.minOrNull() ?: Int.MAX_VALUE
            if (wIdx <= dIdx) "EXPENSE" else "INCOME"
        } else if (isWithdraw) "EXPENSE" else "INCOME"

        var amount = extractAmount(normalized) ?: return null
        var balance = extractBalance(normalized)

        // Rial vs Toman: unit absent -> assume rial (most Iranian banks do).
        val amountIsToman = Regex("""(\d{3,15})\s*(تومان|تومن)""").containsMatchIn(normalized) &&
            !Regex("""(\d{3,15})\s*(ریال|ريال)""").containsMatchIn(normalized)
        if (!amountIsToman) amount /= 10

        val balanceIsToman = balance != null &&
            Regex("""(?:مانده|موجودی)[^\d]{0,15}\d{3,15}\s*(تومان|تومن)""").containsMatchIn(normalized)
        if (balance != null && !balanceIsToman) balance = balance!! / 10

        val last4 = last4Regex.find(normalized)?.groupValues?.get(1)
        val hash = sha1((sender ?: "") + normalized)

        return ParsedSms(
            type = type,
            amount = amount,
            balance = balance,
            accountLast4 = last4,
            rawBody = rawBody,
            normalizedBody = normalized,
            hash = hash
        )
    }

    private fun extractAmount(normalized: String): Long? {
        amountRegex.find(normalized)?.let { return it.groupValues[1].toLongOrNull() }
        // Fallback per spec sec.3: nearest number >= 1000 to a type keyword.
        val keywordIdx = (withdrawKeywords + depositKeywords)
            .mapNotNull { kw -> normalized.indexOf(kw).takeIf { it >= 0 } }
            .minOrNull() ?: return null
        return genericNumberRegex.findAll(normalized)
            .map { it.value to it.range.first }
            .filter { (v, _) -> (v.toLongOrNull() ?: 0) >= 1000 }
            .minByOrNull { (_, pos) -> kotlin.math.abs(pos - keywordIdx) }
            ?.first?.toLongOrNull()
    }

    private fun extractBalance(normalized: String): Long? {
        balanceRegex.find(normalized)?.let { return it.groupValues[1].toLongOrNull() }
        return null
    }

    /**
     * Category precedence per SMS_PARSING.md sec.7:
     * 1. user override (caller's job — checked against a merchant->category map
     *    before falling back to this function)
     * 2. keyword rules (below)
     * 3. default "سایر", except income which defaults to "درآمد"
     */
    fun categorize(type: String, normalizedBodyOrTitle: String): String {
        if (type == "INCOME") return "درآمد"
        for ((cat, keywords) in categoryKeywords) {
            if (keywords.any { normalizedBodyOrTitle.contains(it, ignoreCase = true) }) return cat
        }
        return "سایر"
    }
}
