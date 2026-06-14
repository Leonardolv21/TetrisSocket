package com.example.tetrissocket.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tetrissocket.data.repository.GameRepository
import com.example.tetrissocket.data.repository.SocketRepository
import com.example.tetrissocket.domain.model.MatchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val socketRepository: SocketRepository,
) : ViewModel() {
    private val _result = MutableStateFlow<MatchResult?>(null)
    val result: StateFlow<MatchResult?> = _result.asStateFlow()

    init {
        viewModelScope.launch {
            gameRepository.matchResult.collectLatest { _result.value = it }
        }
    }

    fun finishSession() {
        socketRepository.resetSession()
        gameRepository.clearMatchResult()
    }
}
