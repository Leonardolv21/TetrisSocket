package com.example.tetrissocket.ui.screens.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tetrissocket.ui.components.GameControls
import com.example.tetrissocket.ui.components.NextPiecePreview
import com.example.tetrissocket.ui.components.TetrisBoard
import com.example.tetrissocket.ui.uistate.GameUiState

@Composable
fun GameScreen(
    state: GameUiState,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onRotate: () -> Unit,
    onSoftDrop: () -> Unit,
    onHardDrop: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Sala ${state.roomCode}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(text = "Conexion: ${state.connectionStatus.name}")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(2f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TetrisBoard(state = state.localGameState)
                GameControls(
                    onLeft = onMoveLeft,
                    onRight = onMoveRight,
                    onRotate = onRotate,
                    onSoftDrop = onSoftDrop,
                    onHardDrop = onHardDrop,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                InfoCard(title = "Siguiente") {
                    NextPiecePreview(
                        kind = state.localGameState.nextPiece,
                        cells = state.localGameState.nextPieceCells,
                    )
                }
                InfoCard(title = "Puntaje") {
                    Text(text = state.localGameState.score.toString())
                }
                InfoCard(title = "Lineas") {
                    Text(text = state.localGameState.linesCleared.toString())
                }
                InfoCard(title = "Oponente") {
                    Text(text = state.opponentStatusLabel)
                }
                InfoCard(title = "Duracion") {
                    Text(text = "${state.durationSeconds}s")
                }
                InfoCard(title = "Referencia 37") {
                    Text(text = "Canal 37 activo")
                }
            }
        }

        state.lastError?.let {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                    Button(onClick = onDismissError) {
                        Text("Ocultar")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            content()
        }
    }
}
