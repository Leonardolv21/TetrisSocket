package com.example.tetrissocket.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tetrissocket.domain.model.LocalGameState

@Composable
fun TetrisBoard(
    state: LocalGameState,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.5f)
            .border(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        val cellWidth = size.width / LocalGameState.BOARD_WIDTH
        val cellHeight = size.height / LocalGameState.BOARD_HEIGHT

        drawRect(color = Color(0xFF10151E))

        state.board.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { columnIndex, block ->
                if (block != null) {
                    drawBlock(
                        color = blockColor(block),
                        x = columnIndex * cellWidth,
                        y = rowIndex * cellHeight,
                        width = cellWidth,
                        height = cellHeight,
                    )
                }
            }
        }

        state.activeCells.forEach { (row, column) ->
            if (row >= 0) {
                drawBlock(
                    color = blockColor(state.activePiece?.kind ?: state.nextPiece),
                    x = column * cellWidth,
                    y = row * cellHeight,
                    width = cellWidth,
                    height = cellHeight,
                )
            }
        }

        for (row in 1 until LocalGameState.BOARD_HEIGHT) {
            drawLine(
                color = Color(0xFF263238),
                start = Offset(0f, row * cellHeight),
                end = Offset(size.width, row * cellHeight),
                strokeWidth = 1f,
            )
        }
        for (column in 1 until LocalGameState.BOARD_WIDTH) {
            drawLine(
                color = Color(0xFF263238),
                start = Offset(column * cellWidth, 0f),
                end = Offset(column * cellWidth, size.height),
                strokeWidth = 1f,
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBlock(
    color: Color,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
) {
    drawRect(
        color = color,
        topLeft = Offset(x + 2f, y + 2f),
        size = Size(width - 4f, height - 4f),
    )
}
