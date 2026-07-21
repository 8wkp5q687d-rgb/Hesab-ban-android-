package com.bezz.hesabban

import android.content.Context
import android.provider.Telephony

/**
 * One-shot scan of the SMS inbox for historical bank messages from
 * allow-listed senders only, so the app has data from before it was
 * installed. Per SMS_PARSING.md sec.8 — requires READ_SMS permission and
 * explicit user consent (triggered by a button tap, never automatic).
 */
object SmsImporter {

    suspend fun importInboxHistory(context: Context, monthsBack: Int = 3): Int {
        var count = 0
        val cutoff = System.currentTimeMillis() - monthsBack * 30L * 24 * 60 * 60 * 1000

        val uri = Telephony.Sms.Inbox.CONTENT_URI
        val projection = arrayOf(Telephony.Sms.BODY, Telephony.Sms.ADDRESS, Telephony.Sms.DATE)

        context.contentResolver.query(
            uri, projection,
            "${Telephony.Sms.DATE} >= ?",
            arrayOf(cutoff.toString()),
            "${Telephony.Sms.DATE} DESC"
        )?.use { cursor ->
            val bodyIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val addrIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val dateIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)

            while (cursor.moveToNext()) {
                val body = cursor.getString(bodyIdx) ?: continue
                val addr = cursor.getString(addrIdx)
                val date = cursor.getLong(dateIdx)

                if (Ingest.processSms(context, addr, body, date)) count++
            }
        }
        return count
    }
}
