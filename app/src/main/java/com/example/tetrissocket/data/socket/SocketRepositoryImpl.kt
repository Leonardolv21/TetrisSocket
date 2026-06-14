package com.example.tetrissocket.data.socket

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
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    override val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _roomSession = MutableStateFlow(RoomSession())
    override val roomSession: StateFlow<RoomSession> = _roomSession.asStateFlow()

    private val _events = MutableSharedFlow<SocketEvent>(extraBufferCapacity = 8)
    override val events: SharedFlow<SocketEvent> = _events.asSharedFlow()

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
        socket?.emit("create_room")
    }

    override fun joinRoom(roomCode: String) {
        connect()
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
        val payload = JSONObject()
            .put("roomId", roomCode)
            .put("garbageLines", garbageLines)
        socket?.emit("send_attack", payload)
    }

    override fun sendGameOver(roomCode: String) {
        val payload = JSONObject().put("roomId", roomCode)
        socket?.emit("game_over", payload)
    }

    override fun resetSession() {
        _roomSession.value = RoomSession()
        disconnect()
    }

    private fun createSocket() {
        val options = IO.Options.builder()
            .setForceNew(true)
            .setReconnection(true)
            .build()
        socket = IO.socket(ServerConfig.SERVER_URL, options).apply {
            on(Socket.EVENT_CONNECT) {
                _connectionStatus.value = ConnectionStatus.CONNECTED
            }
            on(Socket.EVENT_CONNECT_ERROR) {
                _connectionStatus.value = ConnectionStatus.RECONNECTING
            }
            on(Manager.EVENT_RECONNECT_ATTEMPT) {
                _connectionStatus.value = ConnectionStatus.RECONNECTING
            }
            on(Socket.EVENT_DISCONNECT) {
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
            }
            on("room_created") { args ->
                val data = args.firstOrNull() as? JSONObject ?: return@on
                val roomCode = data.optString("roomId")
                _roomSession.value = RoomSession(
                    roomCode = roomCode,
                    isHost = true,
                    playersConnected = 1,
                    gameStarted = false,
                )
                _events.tryEmit(SocketEvent.RoomCreated(roomCode))
            }
            on("game_start") {
                val current = _roomSession.value
                _roomSession.value = current.copy(playersConnected = 2, gameStarted = true)
                _events.tryEmit(SocketEvent.GameStarted)
            }
            on("receive_attack") { args ->
                val data = args.firstOrNull() as? JSONObject ?: return@on
                val garbageLines = data.optInt("garbageLines", 0)
                _events.tryEmit(SocketEvent.AttackReceived(garbageLines))
            }
            on("victory") {
                _events.tryEmit(SocketEvent.Victory)
            }
            on("opponent_disconnected") {
                _events.tryEmit(SocketEvent.OpponentDisconnected)
            }
            on("error_message") { args ->
                val data = args.firstOrNull() as? JSONObject ?: return@on
                _events.tryEmit(SocketEvent.Error(data.optString("message", "Unknown error")))
            }
        }
    }
}
