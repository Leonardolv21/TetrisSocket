package com.example.tetrissocket.data.socket

import android.util.Log
import com.example.tetrissocket.data.repository.SocketRepository
import com.example.tetrissocket.domain.model.ConnectionStatus
import com.example.tetrissocket.domain.model.RoomSession
import com.example.tetrissocket.domain.model.SocketEvent
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketRepositoryImpl @Inject constructor() : SocketRepository {
    companion object {
        private const val TAG = "TetrisSocket"
    }

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    override val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _roomSession = MutableStateFlow(RoomSession())
    override val roomSession: StateFlow<RoomSession> = _roomSession.asStateFlow()

    private val _events = MutableSharedFlow<SocketEvent>(extraBufferCapacity = 8)
    override val events: SharedFlow<SocketEvent> = _events.asSharedFlow()

    private val pendingGarbageLock = Any()
    private val _pendingGarbageLines = MutableStateFlow(0)
    override val pendingGarbageLines: StateFlow<Int> = _pendingGarbageLines.asStateFlow()

    private var socket: Socket? = null

    override fun connect() {
        if (socket == null) {
            createSocket()
        }
        val currentSocket = socket ?: return
        if (!currentSocket.connected()) {
            _connectionStatus.value = ConnectionStatus.CONNECTING
            currentSocket.connect()
        }
    }

    override fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
    }

    override fun createRoom() {
        connect()
        Log.d(TAG, "Emitiendo create_room")
        socket?.emit("create_room")
    }

    override fun joinRoom(roomCode: String) {
        connect()
        Log.d(TAG, "Emitiendo join_room roomId=$roomCode")
        _roomSession.value = RoomSession(
            roomCode = roomCode,
            isHost = false,
            playersConnected = 1,
            gameStarted = false,
        )
        val payload = JSONObject().put("roomId", roomCode)
        socket?.emit("join_room", payload)
    }

    override fun sendAttack(roomCode: String, garbageLines: Int) {
        if (garbageLines <= 0) return
        Log.d(TAG, "Emitiendo send_attack roomId=$roomCode garbageLines=$garbageLines")
        val payload = JSONObject()
            .put("roomId", roomCode)
            .put("garbageLines", garbageLines)
        socket?.emit("send_attack", payload)
    }

    override fun sendGameOver(roomCode: String) {
        Log.d(TAG, "Emitiendo game_over roomId=$roomCode")
        val payload = JSONObject().put("roomId", roomCode)
        socket?.emit("game_over", payload)
    }

    override fun consumePendingGarbageLines(): Int {
        synchronized(pendingGarbageLock) {
            val pendingLines = _pendingGarbageLines.value
            _pendingGarbageLines.value = 0
            return pendingLines
        }
    }

    override fun resetSession() {
        _roomSession.value = RoomSession()
        _pendingGarbageLines.value = 0
        disconnect()
    }

    private fun createSocket() {
        val options = IO.Options.builder()
            .setForceNew(true)
            .setReconnection(true)
            .build()
        socket = IO.socket(ServerConfig.SERVER_URL, options).apply {
            on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket conectado")
                _connectionStatus.value = ConnectionStatus.CONNECTED
            }
            on(Socket.EVENT_CONNECT_ERROR) {
                Log.d(TAG, "Socket connect_error")
                _connectionStatus.value = ConnectionStatus.RECONNECTING
            }
            on(Manager.EVENT_RECONNECT_ATTEMPT) {
                Log.d(TAG, "Socket reconnect_attempt")
                _connectionStatus.value = ConnectionStatus.RECONNECTING
            }
            on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "Socket desconectado")
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
            }
            on("room_created") { args ->
                val data = args.firstOrNull() as? JSONObject ?: return@on
                val roomCode = data.optString("roomId")
                Log.d(TAG, "Evento room_created roomId=$roomCode")
                _roomSession.value = RoomSession(
                    roomCode = roomCode,
                    isHost = true,
                    playersConnected = 1,
                    gameStarted = false,
                )
                _events.tryEmit(SocketEvent.RoomCreated(roomCode))
            }
            on("game_start") {
                Log.d(TAG, "Evento game_start")
                val current = _roomSession.value
                _roomSession.value = current.copy(playersConnected = 2, gameStarted = true)
                _events.tryEmit(SocketEvent.GameStarted)
            }
            on("receive_attack") { args ->
                val data = args.firstOrNull() as? JSONObject ?: return@on
                val garbageLines = data.optInt("garbageLines", 0)
                Log.d(TAG, "Evento receive_attack garbageLines=$garbageLines")
                synchronized(pendingGarbageLock) {
                    _pendingGarbageLines.value += garbageLines
                }
                _events.tryEmit(SocketEvent.AttackReceived(garbageLines))
            }
            on("victory") {
                Log.d(TAG, "Evento victory")
                _events.tryEmit(SocketEvent.Victory)
            }
            on("opponent_disconnected") {
                Log.d(TAG, "Evento opponent_disconnected")
                _events.tryEmit(SocketEvent.OpponentDisconnected)
            }
            on("error_message") { args ->
                val data = args.firstOrNull() as? JSONObject ?: return@on
                val message = data.optString("message", "Unknown error")
                Log.d(TAG, "Evento error_message=$message")
                _events.tryEmit(SocketEvent.Error(message))
            }
        }
    }
}
