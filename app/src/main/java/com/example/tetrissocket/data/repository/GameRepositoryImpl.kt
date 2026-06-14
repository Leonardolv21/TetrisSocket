package com.example.tetrissocket.data.repository

import com.example.tetrissocket.domain.game.EngineStepResult
import com.example.tetrissocket.domain.game.TetrisEngine
import com.example.tetrissocket.domain.model.LocalGameState
import com.example.tetrissocket.domain.model.MatchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepositoryImpl @Inject constructor() : GameRepository {
    private val engine = TetrisEngine()

    private val _gameState = MutableStateFlow(engine.snapshot())
    override val gameState: StateFlow<LocalGameState> = _gameState.asStateFlow()

    private val _matchResult = MutableStateFlow<MatchResult?>(null)
    override val matchResult: StateFlow<MatchResult?> = _matchResult.asStateFlow()

    override fun startNewGame() {
        engine.reset()
        _matchResult.value = null
        updateState()
    }

    override fun moveLeft() {
        engine.moveLeft()
        updateState()
    }

    override fun moveRight() {
        engine.moveRight()
        updateState()
    }

    override fun rotate() {
        engine.rotate()
        updateState()
    }

    override fun softDrop(): EngineStepResult {
        return engine.softDrop().also { updateState() }
    }

    override fun hardDrop(): EngineStepResult {
        return engine.hardDrop().also { updateState() }
    }

    override fun tick(): EngineStepResult {
        return engine.tick().also { updateState() }
    }

    override fun applyGarbage(lines: Int): EngineStepResult {
        return engine.applyGarbage(lines).also { updateState() }
    }

    override fun currentState(): LocalGameState = _gameState.value

    override fun saveMatchResult(result: MatchResult) {
        _matchResult.value = result
    }

    override fun clearMatchResult() {
        _matchResult.value = null
    }

    private fun updateState() {
        _gameState.value = engine.snapshot()
    }
}
