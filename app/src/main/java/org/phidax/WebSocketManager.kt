package org.phidax

import okhttp3.*
import org.json.JSONObject
import java.util.UUID

class WebSocketManager(
    private val onMessageReceived: (String) -> Unit,
    private val onStatusChanged: (Boolean) -> Unit
) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()
    private val sessionId = UUID.randomUUID().toString()

    fun connect() {
        val request = Request.Builder()
            .url("wss://maximusprime.duckdns.org/phidax")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                onStatusChanged(true)
                // Send auth token
                val auth = JSONObject().apply {
                    put("token", "PHIDAX_TOKEN_2026")
                }
                webSocket.send(auth.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    when (json.getString("type")) {
                        "response" -> {
                            val content = json.getString("content")
                            onMessageReceived(content)
                        }
                        "error" -> {
                            val error = json.optString("error", "Erreur inconnue")
                            onMessageReceived("❌ $error")
                        }
                    }
                } catch (e: Exception) {
                    onMessageReceived("⚠️ Erreur parsing: ${e.message}")
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                onStatusChanged(false)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                onStatusChanged(false)
                onMessageReceived("❌ Connexion perdue: ${t.message}")
            }
        })
    }

    fun sendMessage(prompt: String) {
        val message = JSONObject().apply {
            put("type", "prompt")
            put("session_id", sessionId)
            put("prompt", prompt)
        }
        webSocket?.send(message.toString())
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnect")
        webSocket = null
    }
}
