package com.example.tetrissocket.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tetrissocket.data.repository.GameRepository
import com.example.tetrissocket.data.repository.SocketRepository
import com.example.tetrissocket.domain.model.MatchEndReason
import com.example.tetrissocket.domain.model.MatchOutcome
import com.example.tetrissocket.domain.model.MatchResult
import com.example.tetrissocket.domain.model.SocketEvent
import com.example.tetrissocket.ui.uistate.GameNavigationEvent
import com.example.tetrissocket.ui.uistate.GameUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class GameViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gameRepository: GameRepository,
    private val socketRepository: SocketRepository,
) : ViewModel() {
    private val roomCode: String = savedStateHandle["roomCode"] ?: ""

    private val _uiState = MutableStateFlow(GameUiState(roomCode = roomCode))
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<GameNavigationEvent>()
    val navigationEvents: SharedFlow<GameNavigationEvent> = _navigationEvents.asSharedFlow()

    private var dropJob: Job? = null
    private var timerJob: Job? = null
    private var startTimeMillis: Long = 0L
    private var matchFinished = false

    init {
        gameRepository.startNewGame()
        startTimeMillis = System.currentTimeMillis()
        observeRepositories()
        startTimers()
    }

    fun moveLeft() = gameRepository.moveLeft()

    fun moveRight() = gameRepository.moveRight()

    fun rotate() = gameRepository.rotate()

    fun softDrop() {
        processEngineStep(gameRepository.softDrop())
    }

    fun hardDrop() {
        processEngineStep(gameRepository.hardDrop())
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(lastError = null)
    }

    fun finishAndReturnToHome() {
        socketRepository.resetSession()
        gameRepository.clearMatchResult()
    }

    private fun observeRepositories() {
        viewModelScope.launch {
            gameRepository.gameState.collectLatest { localState ->
                _uiState.value = _uiState.value.copy(localGameState = localState)
                if (localState.isGameOver && !matchFinished) {
                    finishMatch(
                        outcome = MatchOutcome.LOSS,
                        reason = MatchEndReason.TOP_OUT,
                        notifyServer = true,
                    )
                }
            }
        }

        viewModelScope.launch {
            socketRepository.connectionStatus.collectLatest { status ->
                _uiState.value = _uiState.value.copy(connectionStatus = status)
            }
        }

        viewModelScope.launch {
            socketRepository.events.collectLatest { event ->
                when (event) {
                    is SocketEvent.AttackReceived -> {
                        processEngineStep(gameRepository.applyGarbage(event.garbageLines))
                    }
                    SocketEvent.Victory -> {
                        finishMatch(
                            outcome = MatchOutcome.WIN,
                            reason = MatchEndReason.OPPONENT_TOP_OUT,
                            notifyServer = false,
                        )
                    }
                    SocketEvent.OpponentDisconnected -> {
                        _uiState.value = _uiState.value.copy(opponentStatusLabel = "Desconectado")
                        finishMatch(
                            outcome = MatchOutcome.WIN,
                            reason = MatchEndReason.OPPONENT_DISCONNECTED,
                            notifyServer = false,
                        )
                    }
                    is SocketEvent.Error -> {
                        _uiState.value = _uiState.value.copy(lastError = event.message)
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun startTimers() {
        _uiState.value = _uiState.value.copy(opponentStatusLabel = "Activo")

        dropJob = viewModelScope.launch {
            while (!matchFinished) {
                delay(650)
                processEngineStep(gameRepository.tick())
            }
        }

        timerJob = viewModelScope.launch {
            while (!matchFinished) {
                delay(1000)
                val elapsed = (System.currentTimeMillis() - startTimeMillis) / 1000
                _uiState.value = _uiState.value.copy(durationSeconds = elapsed)
            }
        }
    }

    private fun processEngineStep(stepResult: com.example.tetrissocket.domain.game.EngineStepResult) {
        if (stepResult.sentGarbage > 0) {
            socketRepository.sendAttack(roomCode = roomCode, garbageLines = stepResult.sentGarbage)
        }
        if (stepResult.didLose && !matchFinished) {
            finishMatch(
                outcome = MatchOutcome.LOSS,
                reason = MatchEndReason.TOP_OUT,
                notifyServer = true,
            )
        }
    }

    private fun finishMatch(
        outcome: MatchOutcome,
        reason: MatchEndReason,
        notifyServer: Boolean,
    ) {
        if (matchFinished) return
        matchFinished = true
        dropJob?.cancel()
        timerJob?.cancel()

        if (notifyServer) {
            socketRepository.sendGameOver(roomCode)
        }

        _uiState.value = _uiState.value.copy(
            opponentStatusLabel = when (outcome) {
                MatchOutcome.WIN -> "Derrotado"
                MatchOutcome.LOSS -> "Activo"
            }
        )

        val currentState = gameRepository.currentState()
        val result = MatchResult(
            outcome = outcome,
            score = currentState.score,
            linesCleared = currentState.linesCleared,
            durationSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000,
            reason = reason,
        )
        gameRepository.saveMatchResult(result)

        viewModelScope.launch {
            _navigationEvents.emit(GameNavigationEvent.NavigateToResult)
        }
    }
}
