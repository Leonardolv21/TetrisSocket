package com.example.tetrissocket.domain.model

data class ActivePiece(
    val kind: BlockKind,
    val row: Int,
    val column: Int,
    val rotation: Int,
)
