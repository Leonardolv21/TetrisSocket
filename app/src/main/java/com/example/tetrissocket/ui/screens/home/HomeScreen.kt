package com.example.tetrissocket.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tetrissocket.domain.model.ConnectionStatus
import com.example.tetrissocket.ui.uistate.LobbyUiState

@Composable
fun HomeScreen(
    state: LobbyUiState,
    onRoomCodeChange: (String) -> Unit,
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Tetris Duel Online",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Conexion: ${state.connectionStatus.name}",
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(
            onClick = onCreateRoom,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.connectionStatus != ConnectionStatus.CONNECTING,
        ) {
            Text("Crear sala")
        }
        OutlinedTextField(
            value = state.roomCodeInput,
            onValueChange = onRoomCodeChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Codigo de sala") },
            singleLine = true,
        )
        Button(
            onClick = onJoinRoom,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Unirse a sala")
        }
        state.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(onClick = onDismissError) {
                Text("Ocultar")
            }
        }
        Text(
            text = "Configura tu IP del servidor en ServerConfig antes de probar en celular o emulador.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
