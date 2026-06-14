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
import com.example.tetrissocket.domain.model.BlockKind

@Composable
fun NextPiecePreview(
    kind: BlockKind,
    cells: List<Pair<Int, Int>>,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        drawRect(Color(0xFF151B25))
        val cellWidth = size.width / 4f
        val cellHeight = size.height / 4f

        cells.forEach { (row, column) ->
            drawRect(
                color = blockColor(kind),
                topLeft = Offset(column * cellWidth + 4f, row * cellHeight + 4f),
                size = Size(cellWidth - 8f, cellHeight - 8f),
            )
        }
    }
}
