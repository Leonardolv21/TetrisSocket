package com.example.tetrissocket.domain.model

data class RoomSession(
    val roomCode: String = "",
    val isHost: Boolean = false,
    val playersConnected: Int = 1,
    val gameStarted: Boolean = false,
)
