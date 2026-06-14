package com.example.tetrissocket.ui.navigation

object TetrisDestination {
    const val HOME = "home"
    const val LOBBY = "lobby/{roomCode}/{isHost}"
    const val GAME = "game/{roomCode}"
    const val RESULT = "result"

    fun lobby(roomCode: String, isHost: Boolean): String = "lobby/$roomCode/$isHost"
    fun game(roomCode: String): String = "game/$roomCode"
}
