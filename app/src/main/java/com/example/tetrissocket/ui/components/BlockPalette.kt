package com.example.tetrissocket.ui.components

import androidx.compose.ui.graphics.Color
import com.example.tetrissocket.domain.model.BlockKind

fun blockColor(kind: BlockKind): Color {
    return when (kind) {
        BlockKind.I -> Color(0xFF4DD0E1)
        BlockKind.O -> Color(0xFFFFF176)
        BlockKind.T -> Color(0xFFBA68C8)
        BlockKind.S -> Color(0xFF81C784)
        BlockKind.Z -> Color(0xFFE57373)
        BlockKind.J -> Color(0xFF64B5F6)
        BlockKind.L -> Color(0xFFFFB74D)
        BlockKind.GARBAGE -> Color(0xFF050505)
    }
}
