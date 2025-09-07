package com.singularis.eateria.services

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class FriendsSearchWebSocket(
    private val authTokenProvider: suspend () -> String?,
) {
    enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED, AUTHENTICATED, FAILED }

    private val client: OkHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    private val url = "wss://chater.singularis.work/autocomplete"
    private var webSocket: WebSocket? = null
    private var scope: CoroutineScope? = null

    private val _state = MutableStateFlow(ConnectionState.DISCONNECTED)
    val state: StateFlow<ConnectionState> = _state

    private val _resultsChannel = Channel<List<String>>(Channel.CONFLATED)
    val resultsChannel: Channel<List<String>> = _resultsChannel

    fun attachScope(scope: CoroutineScope) {
        this.scope = scope
    }

    fun connect() {
        if (webSocket != null) return
        _state.value = ConnectionState.CONNECTING
        val request =
            Request
                .Builder()
                .url(url)
                .build()
        webSocket = client.newWebSocket(request, Listener())
    }

    fun disconnect() {
        webSocket?.close(1000, null)
        webSocket = null
        _state.value = ConnectionState.DISCONNECTED
    }

    fun search(
        query: String,
        limit: Int = 10,
    ) {
        if (query.trim().isEmpty()) return
        connect()
        sendAuthIfNeeded()
        val payload =
            JSONObject()
                .put("type", "search")
                .put("query", query)
                .put("limit", limit)
        send(payload)
    }

    private fun sendAuthIfNeeded() {
        if (_state.value == ConnectionState.AUTHENTICATED || _state.value == ConnectionState.FAILED) return
        if (_state.value == ConnectionState.CONNECTED) {
            scope?.launch(Dispatchers.IO) {
                val token = authTokenProvider()
                if (token.isNullOrEmpty()) {
                    _state.value = ConnectionState.FAILED
                    return@launch
                }
                val payload =
                    JSONObject()
                        .put("type", "auth")
                        .put("token", token)
                send(payload)
            }
        }
    }

    private fun send(json: JSONObject) {
        val text = json.toString()
        webSocket?.send(text)
    }

    private inner class Listener : WebSocketListener() {
        override fun onOpen(
            webSocket: WebSocket,
            response: okhttp3.Response,
        ) {
            _state.value = ConnectionState.CONNECTED
            sendAuthIfNeeded()
        }

        override fun onMessage(
            webSocket: WebSocket,
            text: String,
        ) {
            try {
                val obj = JSONObject(text)
                val type = obj.optString("type")
                if (type == "connection") {
                    if (obj.optString("status") == "connected") {
                        _state.value = ConnectionState.AUTHENTICATED
                    }
                } else if (type == "results") {
                    val results = obj.optJSONArray("results") ?: JSONArray()
                    val emails = mutableListOf<String>()
                    for (i in 0 until results.length()) {
                        val item = results.optJSONObject(i)
                        val email = item?.optString("email")
                        if (!email.isNullOrEmpty()) emails.add(email)
                    }
                    scope?.launch { _resultsChannel.send(emails) }
                }
            } catch (e: Exception) {
                Log.e("FriendsWS", "Failed to parse message", e)
            }
        }

        override fun onMessage(
            webSocket: WebSocket,
            bytes: ByteString,
        ) {
            onMessage(webSocket, bytes.utf8())
        }

        override fun onFailure(
            webSocket: WebSocket,
            t: Throwable,
            response: okhttp3.Response?,
        ) {
            _state.value = ConnectionState.FAILED
            disconnect()
        }
    }
}
