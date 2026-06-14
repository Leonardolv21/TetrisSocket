package com.example.tetrissocket.ui.screens.result

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
import com.example.tetrissocket.domain.model.MatchOutcome
import com.example.tetrissocket.domain.model.MatchResult

@Composable
fun ResultScreen(
    result: MatchResult?,
    onBackHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Resultado",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        if (result == null) {
            Text(text = "No hay resultado disponible")
        } else {
            Text(text = if (result.outcome == MatchOutcome.WIN) "Ganador: Tu" else "Ganador: Oponente")
            Text(text = "Puntaje: ${result.score}")
            Text(text = "Lineas eliminadas: ${result.linesCleared}")
            Text(text = "Duracion: ${result.durationSeconds}s")
            Text(text = "Motivo: ${result.reason.name}")
        }

        Button(
            onClick = onBackHome,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Volver al inicio")
        }
    }
}
