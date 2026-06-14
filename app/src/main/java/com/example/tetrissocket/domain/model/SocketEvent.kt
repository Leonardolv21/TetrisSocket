package com.example.tetrissocket.domain.model

sealed interface SocketEvent {
    data class RoomCreated(val roomCode: String) : SocketEvent
    data object GameStarted : SocketEvent
    data class AttackReceived(val garbageLines: Int) : SocketEvent
    data object Victory : SocketEvent
    data object OpponentDisconnected : SocketEvent
    data class Error(val message: String) : SocketEvent
}
