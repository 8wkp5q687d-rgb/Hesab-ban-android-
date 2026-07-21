package com.bezz.hesabban

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Listens for incoming SMS in real time. If the sender is on the allow-list
 * and the message looks like a bank transaction, it's parsed and stored in
 * the local Room DB via Ingest. Runs entirely on-device — nothing is sent
 * anywhere, no network permission at all. [قطعی]
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val pending = goAsync()
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val fullBody = messages.joinToString(separator = "") { it.messageBody ?: "" }
        val sender = messages.firstOrNull()?.originatingAddress
        val date = System.currentTimeMillis()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Ingest.processSms(context, sender, fullBody, date)
                // TODO: post a notification on successful insert, per ANDROID_GUIDE.md
            } finally {
                pending.finish()
            }
        }
    }
}
