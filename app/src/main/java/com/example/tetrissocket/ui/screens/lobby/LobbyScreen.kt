package com.example.tetrissocket.ui.screens.lobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tetrissocket.ui.uistate.LobbyUiState

@Composable
fun LobbyScreen(
    state: LobbyUiState,
    onLeaveLobby: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = if (state.isHost) "Sala creada" else "Uniendose a sala",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(text = "Codigo: ${state.activeRoomCode}")
        Text(text = "Conexion: ${state.connectionStatus.name}")
        Text(text = "Jugadores conectados: ${state.playersConnected}/2")
        Text(
            text = if (state.isWaitingForOpponent) {
                "Esperando al segundo jugador. La partida inicia automaticamente."
            } else {
                "Partida lista. Iniciando..."
            }
        )
        state.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = onLeaveLobby,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Salir")
        }
    }
}
