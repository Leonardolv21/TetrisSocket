package com.example.tetrissocket.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tetrissocket.data.repository.SocketRepository
import com.example.tetrissocket.domain.model.SocketEvent
import com.example.tetrissocket.ui.uistate.LobbyNavigationEvent
import com.example.tetrissocket.ui.uistate.LobbyUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val socketRepository: SocketRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LobbyUiState())
    val uiState: StateFlow<LobbyUiState> = _uiState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<LobbyNavigationEvent>()
    val navigationEvents: SharedFlow<LobbyNavigationEvent> = _navigationEvents.asSharedFlow()

    init {
        socketRepository.connect()

        viewModelScope.launch {
            socketRepository.connectionStatus.collectLatest { status ->
                _uiState.value = _uiState.value.copy(connectionStatus = status)
            }
        }

        viewModelScope.launch {
            socketRepository.roomSession.collectLatest { session ->
                if (session.roomCode.isBlank()) return@collectLatest
                _uiState.value = _uiState.value.copy(
                    activeRoomCode = session.roomCode,
                    isHost = session.isHost,
                    playersConnected = session.playersConnected,
                    isWaitingForOpponent = !session.gameStarted,
                )
            }
        }

        viewModelScope.launch {
            socketRepository.events.collectLatest { event ->
                when (event) {
                    is SocketEvent.RoomCreated -> {
                        _navigationEvents.emit(
                            LobbyNavigationEvent.NavigateToLobby(
                                roomCode = event.roomCode,
                                isHost = true,
                            )
                        )
                    }
                    SocketEvent.GameStarted -> {
                        _navigationEvents.emit(
                            LobbyNavigationEvent.NavigateToGame(_uiState.value.activeRoomCode)
                        )
                    }
                    is SocketEvent.Error -> {
                        _uiState.value = _uiState.value.copy(errorMessage = event.message)
                    }
                    else -> Unit
                }
            }
        }
    }

    fun onRoomCodeInputChange(value: String) {
        _uiState.value = _uiState.value.copy(roomCodeInput = value.uppercase())
    }

    fun createRoom() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
        socketRepository.createRoom()
    }

    fun joinRoom() {
        val roomCode = _uiState.value.roomCodeInput.trim().uppercase()
        if (roomCode.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Ingresa el codigo de la sala")
            return
        }
        _uiState.value = _uiState.value.copy(
            activeRoomCode = roomCode,
            isHost = false,
            isWaitingForOpponent = true,
            playersConnected = 1,
            errorMessage = null,
        )
        viewModelScope.launch {
            _navigationEvents.emit(
                LobbyNavigationEvent.NavigateToLobby(
                    roomCode = roomCode,
                    isHost = false,
                )
            )
        }
        socketRepository.joinRoom(roomCode)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetSession() {
        socketRepository.resetSession()
        _uiState.value = LobbyUiState()
    }
}
