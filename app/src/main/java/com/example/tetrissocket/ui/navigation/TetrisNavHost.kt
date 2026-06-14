package com.example.tetrissocket.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tetrissocket.ui.screens.game.GameScreen
import com.example.tetrissocket.ui.screens.home.HomeScreen
import com.example.tetrissocket.ui.screens.lobby.LobbyScreen
import com.example.tetrissocket.ui.screens.result.ResultScreen
import com.example.tetrissocket.ui.uistate.GameNavigationEvent
import com.example.tetrissocket.ui.uistate.LobbyNavigationEvent
import com.example.tetrissocket.ui.viewmodels.GameViewModel
import com.example.tetrissocket.ui.viewmodels.LobbyViewModel
import com.example.tetrissocket.ui.viewmodels.ResultViewModel

@Composable
fun TetrisNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = TetrisDestination.HOME,
    ) {
        composable(TetrisDestination.HOME) {
            val viewModel: LobbyViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.navigationEvents.collect { event ->
                    when (event) {
                        is LobbyNavigationEvent.NavigateToLobby -> {
                            navController.navigate(
                                TetrisDestination.lobby(
                                    roomCode = event.roomCode,
                                    isHost = event.isHost,
                                )
                            )
                        }
                        else -> Unit
                    }
                }
            }

            HomeScreen(
                state = state,
                onRoomCodeChange = viewModel::onRoomCodeInputChange,
                onCreateRoom = viewModel::createRoom,
                onJoinRoom = viewModel::joinRoom,
                onDismissError = viewModel::clearError,
            )
        }

        composable(
            route = TetrisDestination.LOBBY,
            arguments = listOf(
                navArgument("roomCode") { type = NavType.StringType },
                navArgument("isHost") { type = NavType.BoolType },
            ),
        ) {
            val parentEntry = navController.getBackStackEntry(TetrisDestination.HOME)
            val viewModel: LobbyViewModel = hiltViewModel(parentEntry)
            val state by viewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.navigationEvents.collect { event ->
                    if (event is LobbyNavigationEvent.NavigateToGame) {
                        navController.navigate(TetrisDestination.game(event.roomCode)) {
                            popUpTo(TetrisDestination.HOME)
                        }
                    }
                }
            }

            LobbyScreen(
                state = state,
                onLeaveLobby = {
                    viewModel.resetSession()
                    navController.popBackStack(TetrisDestination.HOME, inclusive = false)
                },
            )
        }

        composable(
            route = TetrisDestination.GAME,
            arguments = listOf(navArgument("roomCode") { type = NavType.StringType }),
        ) {
            val viewModel: GameViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.navigationEvents.collect { event ->
                    if (event is GameNavigationEvent.NavigateToResult) {
                        navController.navigate(TetrisDestination.RESULT)
                    }
                }
            }

            GameScreen(
                state = state,
                onMoveLeft = viewModel::moveLeft,
                onMoveRight = viewModel::moveRight,
                onRotate = viewModel::rotate,
                onSoftDrop = viewModel::softDrop,
                onHardDrop = viewModel::hardDrop,
                onDismissError = viewModel::clearError,
            )
        }

        composable(TetrisDestination.RESULT) {
            val resultViewModel: ResultViewModel = hiltViewModel()
            val result by resultViewModel.result.collectAsState()

            ResultScreen(
                result = result,
                onBackHome = {
                    resultViewModel.finishSession()
                    navController.navigate(TetrisDestination.HOME) {
                        popUpTo(TetrisDestination.HOME) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}
