package com.example.tetrissocket.domain.game

import com.example.tetrissocket.domain.model.ActivePiece
import com.example.tetrissocket.domain.model.BlockKind
import com.example.tetrissocket.domain.model.LocalGameState
import kotlin.random.Random

data class EngineStepResult(
    val sentGarbage: Int = 0,
    val didLose: Boolean = false,
)

class TetrisEngine(
    private val random: Random = Random(System.currentTimeMillis()),
) {
    private data class Offset(val row: Int, val column: Int)

    private val playableKinds = listOf(
        BlockKind.I,
        BlockKind.O,
        BlockKind.T,
        BlockKind.S,
        BlockKind.Z,
        BlockKind.J,
        BlockKind.L,
    )

    private val rotations = mapOf(
        BlockKind.I to listOf(
            listOf(Offset(1, 0), Offset(1, 1), Offset(1, 2), Offset(1, 3)),
            listOf(Offset(0, 2), Offset(1, 2), Offset(2, 2), Offset(3, 2)),
            listOf(Offset(2, 0), Offset(2, 1), Offset(2, 2), Offset(2, 3)),
            listOf(Offset(0, 1), Offset(1, 1), Offset(2, 1), Offset(3, 1)),
        ),
        BlockKind.O to List(4) {
            listOf(Offset(0, 1), Offset(0, 2), Offset(1, 1), Offset(1, 2))
        },
        BlockKind.T to listOf(
            listOf(Offset(0, 1), Offset(1, 0), Offset(1, 1), Offset(1, 2)),
            listOf(Offset(0, 1), Offset(1, 1), Offset(1, 2), Offset(2, 1)),
            listOf(Offset(1, 0), Offset(1, 1), Offset(1, 2), Offset(2, 1)),
            listOf(Offset(0, 1), Offset(1, 0), Offset(1, 1), Offset(2, 1)),
        ),
        BlockKind.S to listOf(
            listOf(Offset(0, 1), Offset(0, 2), Offset(1, 0), Offset(1, 1)),
            listOf(Offset(0, 1), Offset(1, 1), Offset(1, 2), Offset(2, 2)),
            listOf(Offset(1, 1), Offset(1, 2), Offset(2, 0), Offset(2, 1)),
            listOf(Offset(0, 0), Offset(1, 0), Offset(1, 1), Offset(2, 1)),
        ),
        BlockKind.Z to listOf(
            listOf(Offset(0, 0), Offset(0, 1), Offset(1, 1), Offset(1, 2)),
            listOf(Offset(0, 2), Offset(1, 1), Offset(1, 2), Offset(2, 1)),
            listOf(Offset(1, 0), Offset(1, 1), Offset(2, 1), Offset(2, 2)),
            listOf(Offset(0, 1), Offset(1, 0), Offset(1, 1), Offset(2, 0)),
        ),
        BlockKind.J to listOf(
            listOf(Offset(0, 0), Offset(1, 0), Offset(1, 1), Offset(1, 2)),
            listOf(Offset(0, 1), Offset(0, 2), Offset(1, 1), Offset(2, 1)),
            listOf(Offset(1, 0), Offset(1, 1), Offset(1, 2), Offset(2, 2)),
            listOf(Offset(0, 1), Offset(1, 1), Offset(2, 0), Offset(2, 1)),
        ),
        BlockKind.L to listOf(
            listOf(Offset(0, 2), Offset(1, 0), Offset(1, 1), Offset(1, 2)),
            listOf(Offset(0, 1), Offset(1, 1), Offset(2, 1), Offset(2, 2)),
            listOf(Offset(1, 0), Offset(1, 1), Offset(1, 2), Offset(2, 0)),
            listOf(Offset(0, 0), Offset(0, 1), Offset(1, 1), Offset(2, 1)),
        ),
    )

    private var board = Array(LocalGameState.BOARD_HEIGHT) {
        arrayOfNulls<BlockKind>(LocalGameState.BOARD_WIDTH)
    }
    private var activePiece: ActivePiece? = null
    private var nextPiece: BlockKind = randomKind()
    private var score: Int = 0
    private var linesCleared: Int = 0
    private var gameOver: Boolean = false

    init {
        spawnNextPiece()
    }

    fun reset() {
        board = Array(LocalGameState.BOARD_HEIGHT) {
            arrayOfNulls<BlockKind>(LocalGameState.BOARD_WIDTH)
        }
        score = 0
        linesCleared = 0
        gameOver = false
        activePiece = null
        nextPiece = randomKind()
        spawnNextPiece()
    }

    fun snapshot(): LocalGameState {
        val boardSnapshot = board.map { row -> row.toList() }
        return LocalGameState(
            board = boardSnapshot,
            activePiece = activePiece,
            activeCells = activePiece?.let { piece -> pieceCells(piece).map { it.row to it.column } } ?: emptyList(),
            nextPiece = nextPiece,
            nextPieceCells = previewOffsets(nextPiece),
            score = score,
            linesCleared = linesCleared,
            isGameOver = gameOver,
        )
    }

    fun tick(): EngineStepResult {
        return moveDownOrLock()
    }

    fun moveLeft() {
        moveBy(deltaRow = 0, deltaColumn = -1)
    }

    fun moveRight() {
        moveBy(deltaRow = 0, deltaColumn = 1)
    }

    fun rotate() {
        val piece = activePiece ?: return
        val rotated = piece.copy(rotation = (piece.rotation + 1) % 4)
        val kicks = listOf(0, -1, 1, -2, 2)
        for (kick in kicks) {
            val candidate = rotated.copy(column = rotated.column + kick)
            if (canPlace(candidate)) {
                activePiece = candidate
                return
            }
        }
    }

    fun softDrop(): EngineStepResult {
        return moveDownOrLock()
    }

    fun hardDrop(): EngineStepResult {
        if (gameOver) return EngineStepResult(didLose = true)
        var moved = false
        while (true) {
            val piece = activePiece ?: break
            val candidate = piece.copy(row = piece.row + 1)
            if (!canPlace(candidate)) {
                break
            }
            activePiece = candidate
            moved = true
        }
        if (moved) {
            score += 2
        }
        return lockPiece()
    }

    fun applyGarbage(lines: Int): EngineStepResult {
        if (gameOver || lines <= 0) return EngineStepResult(didLose = gameOver)
        repeat(lines) {
            val topRowOccupied = board.first().any { it != null }
            if (topRowOccupied) {
                gameOver = true
                return EngineStepResult(didLose = true)
            }

            for (row in 0 until LocalGameState.BOARD_HEIGHT - 1) {
                board[row] = board[row + 1].copyOf()
            }

            val hole = random.nextInt(LocalGameState.BOARD_WIDTH)
            board[LocalGameState.BOARD_HEIGHT - 1] = Array(LocalGameState.BOARD_WIDTH) { column ->
                if (column == hole) null else BlockKind.GARBAGE
            }

            activePiece = activePiece?.copy(row = itSafeSubtract(activePiece?.row ?: 0, 1))
            val piece = activePiece
            if (piece != null && pieceCells(piece).any { cell -> cell.row < 0 }) {
                gameOver = true
                return EngineStepResult(didLose = true)
            }
        }

        if (activePiece != null && !canPlace(activePiece!!)) {
            gameOver = true
        }
        return EngineStepResult(didLose = gameOver)
    }

    private fun itSafeSubtract(value: Int, amount: Int): Int = value - amount

    private fun moveDownOrLock(): EngineStepResult {
        if (gameOver) return EngineStepResult(didLose = true)
        val piece = activePiece ?: return EngineStepResult(didLose = gameOver)
        val candidate = piece.copy(row = piece.row + 1)
        return if (canPlace(candidate)) {
            activePiece = candidate
            EngineStepResult()
        } else {
            lockPiece()
        }
    }

    private fun moveBy(deltaRow: Int, deltaColumn: Int) {
        val piece = activePiece ?: return
        val candidate = piece.copy(
            row = piece.row + deltaRow,
            column = piece.column + deltaColumn,
        )
        if (canPlace(candidate)) {
            activePiece = candidate
        }
    }

    private fun lockPiece(): EngineStepResult {
        val piece = activePiece ?: return EngineStepResult(didLose = gameOver)
        pieceCells(piece).forEach { offset ->
            if (offset.row !in 0 until LocalGameState.BOARD_HEIGHT) {
                gameOver = true
                return EngineStepResult(didLose = true)
            }
            board[offset.row][offset.column] = piece.kind
        }

        val cleared = clearLines()
        linesCleared += cleared
        score += when (cleared) {
            1 -> 100
            2 -> 300
            3 -> 500
            4 -> 800
            else -> 0
        }

        val sentGarbage = when (cleared) {
            1 -> 0
            2 -> 1
            3 -> 2
            4 -> 4
            else -> 0
        }

        activePiece = null
        spawnNextPiece()
        return EngineStepResult(
            sentGarbage = sentGarbage,
            didLose = gameOver,
        )
    }

    private fun clearLines(): Int {
        val remainingRows = board.filterNot { row -> row.all { it != null } }
        val cleared = LocalGameState.BOARD_HEIGHT - remainingRows.size
        if (cleared == 0) return 0

        val newBoard = Array(LocalGameState.BOARD_HEIGHT) {
            arrayOfNulls<BlockKind>(LocalGameState.BOARD_WIDTH)
        }
        val startRow = LocalGameState.BOARD_HEIGHT - remainingRows.size
        remainingRows.forEachIndexed { index, row ->
            newBoard[startRow + index] = row.copyOf()
        }
        board = newBoard
        return cleared
    }

    private fun spawnNextPiece() {
        val kind = nextPiece
        nextPiece = randomKind()
        val spawn = ActivePiece(kind = kind, row = 0, column = 3, rotation = 0)
        if (canPlace(spawn)) {
            activePiece = spawn
        } else {
            activePiece = null
            gameOver = true
        }
    }

    private fun canPlace(piece: ActivePiece): Boolean {
        return pieceCells(piece).all { cell ->
            cell.column in 0 until LocalGameState.BOARD_WIDTH &&
                cell.row < LocalGameState.BOARD_HEIGHT &&
                (cell.row < 0 || board[cell.row][cell.column] == null)
        }
    }

    private fun pieceCells(piece: ActivePiece): List<Offset> {
        val rotationSet = rotations.getValue(piece.kind)[piece.rotation]
        return rotationSet.map { offset ->
            Offset(
                row = piece.row + offset.row,
                column = piece.column + offset.column,
            )
        }
    }

    fun previewOffsets(kind: BlockKind): List<Pair<Int, Int>> {
        return rotations.getValue(kind)[0].map { it.row to it.column }
    }

    private fun randomKind(): BlockKind {
        return playableKinds[random.nextInt(playableKinds.size)]
    }
}
