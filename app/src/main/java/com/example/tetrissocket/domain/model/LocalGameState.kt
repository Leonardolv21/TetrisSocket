package com.example.tetrissocket.domain.model

data class LocalGameState(
    val board: List<List<BlockKind?>>, 
    val activePiece: ActivePiece? = null,
    val activeCells: List<Pair<Int, Int>> = emptyList(),
    val nextPiece: BlockKind = BlockKind.I,
    val nextPieceCells: List<Pair<Int, Int>> = emptyList(),
    val score: Int = 0,
    val linesCleared: Int = 0,
    val isGameOver: Boolean = false,
) {
    companion object {
        const val BOARD_WIDTH = 10
        const val BOARD_HEIGHT = 20

        fun empty(): LocalGameState {
            return LocalGameState(
                board = List(BOARD_HEIGHT) { List<BlockKind?>(BOARD_WIDTH) { null } },
            )
        }
    }
}
