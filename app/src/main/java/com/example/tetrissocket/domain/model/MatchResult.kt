package com.example.tetrissocket.domain.model

enum class MatchOutcome {
    WIN,
    LOSS,
}

enum class MatchEndReason {
    TOP_OUT,
    OPPONENT_TOP_OUT,
    OPPONENT_DISCONNECTED,
}

data class MatchResult(
    val outcome: MatchOutcome,
    val score: Int,
    val linesCleared: Int,
    val durationSeconds: Long,
    val reason: MatchEndReason,
)
