package com.example.tetrissocket.ui.uistate

import com.example.tetrissocket.domain.model.ConnectionStatus

data class LobbyUiState(
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val roomCodeInput: String = "",
    val activeRoomCode: String = "",
    val isHost: Boolean = false,
    val playersConnected: Int = 1,
    val isWaitingForOpponent: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface LobbyNavigationEvent {
    data class NavigateToLobby(val roomCode: String, val isHost: Boolean) : LobbyNavigationEvent
    data class NavigateToGame(val roomCode: String) : LobbyNavigationEvent
}
