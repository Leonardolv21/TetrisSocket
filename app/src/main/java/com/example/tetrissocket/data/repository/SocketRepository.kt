package com.example.tetrissocket.data.repository

import com.example.tetrissocket.domain.model.ConnectionStatus
import com.example.tetrissocket.domain.model.RoomSession
import com.example.tetrissocket.domain.model.SocketEvent
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface SocketRepository {
    val connectionStatus: StateFlow<ConnectionStatus>
    val roomSession: StateFlow<RoomSession>
    val events: SharedFlow<SocketEvent>
    val pendingGarbageLines: StateFlow<Int>

    fun connect()
    fun disconnect()
    fun createRoom()
    fun joinRoom(roomCode: String)
    fun sendAttack(roomCode: String, garbageLines: Int)
    fun sendGameOver(roomCode: String)
    fun consumePendingGarbageLines(): Int
    fun resetSession()
}
