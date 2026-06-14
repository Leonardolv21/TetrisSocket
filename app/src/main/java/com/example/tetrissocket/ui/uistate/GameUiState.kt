package com.example.tetrissocket.ui.uistate

import com.example.tetrissocket.domain.model.ConnectionStatus
import com.example.tetrissocket.domain.model.LocalGameState

data class GameUiState(
    val roomCode: String = "",
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val localGameState: LocalGameState = LocalGameState.empty(),
    val opponentStatusLabel: String = "Esperando",
    val durationSeconds: Long = 0,
    val lastError: String? = null,
)

sealed interface GameNavigationEvent {
    data object NavigateToResult : GameNavigationEvent
}
