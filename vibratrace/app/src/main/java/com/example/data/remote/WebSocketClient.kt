package com.example.data.remote

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.*
import java.util.concurrent.TimeUnit

enum class ConnectionStatus {
    LIVE,
    RECONNECTING,
    OFFLINE,
    DEMO_MODE
}

class WebSocketClient(
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()
) {
    private val _connectionState = MutableStateFlow(ConnectionStatus.OFFLINE)
    val connectionState: StateFlow<ConnectionStatus> = _connectionState

    private val _incomingMessages = MutableStateFlow<String?>(null)
    val incomingMessages: StateFlow<String?> = _incomingMessages

    private var webSocket: WebSocket? = null
    private var isManualDisconnect = false
    private var reconnectJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun setDemoMode() {
        _connectionState.value = ConnectionStatus.DEMO_MODE
    }

    fun connect(wsUrl: String) {
        if (_connectionState.value == ConnectionStatus.DEMO_MODE) return
        isManualDisconnect = false
        _connectionState.value = ConnectionStatus.RECONNECTING

        val request = Request.Builder().url(wsUrl).build()
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.value = ConnectionStatus.LIVE
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                _incomingMessages.value = text
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                _connectionState.value = ConnectionStatus.OFFLINE
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionState.value = ConnectionStatus.OFFLINE
                if (!isManualDisconnect) {
                    scheduleReconnect(wsUrl)
                }
            }
        })
    }

    private fun scheduleReconnect(wsUrl: String) {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            _connectionState.value = ConnectionStatus.RECONNECTING
            delay(5000) // backoff
            if (!isManualDisconnect && _connectionState.value != ConnectionStatus.DEMO_MODE) {
                connect(wsUrl)
            }
        }
    }

    fun disconnect() {
        isManualDisconnect = true
        reconnectJob?.cancel()
        webSocket?.close(1000, "Normal closure")
        _connectionState.value = ConnectionStatus.OFFLINE
    }
}
