package com.example.tetrissocket.data.repository

import com.example.tetrissocket.domain.game.EngineStepResult
import com.example.tetrissocket.domain.model.LocalGameState
import com.example.tetrissocket.domain.model.MatchResult
import kotlinx.coroutines.flow.StateFlow

interface GameRepository {
    val gameState: StateFlow<LocalGameState>
    val matchResult: StateFlow<MatchResult?>

    fun startNewGame()
    fun moveLeft()
    fun moveRight()
    fun rotate()
    fun softDrop(): EngineStepResult
    fun hardDrop(): EngineStepResult
    fun tick(): EngineStepResult
    fun applyGarbage(lines: Int): EngineStepResult
    fun currentState(): LocalGameState
    fun saveMatchResult(result: MatchResult)
    fun clearMatchResult()
}
