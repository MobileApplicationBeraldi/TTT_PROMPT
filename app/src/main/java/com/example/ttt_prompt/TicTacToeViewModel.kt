package com.example.ttt_prompt

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ttt_prompt.domain.usecase.*
import kotlinx.coroutines.launch

class TicTacToeViewModel(
    private val getAgentMoveUseCase: GetAgentMoveUseCase = GetRemoteAgentMoveUseCase(),
    private val checkWinConditionUseCase: CheckWinConditionUseCase = CheckWinConditionUseCaseImpl(),
    private val postTurnUseCase: PostTurnUseCase = PostTurnUseCaseImpl(),
    private val resetGameUseCase: ResetGameUseCase = ResetGameUseCaseImpl()
) : ViewModel() {
    var board by mutableStateOf(List(3) { List(3) { "" } })
        private set

    var isBoardEnabled by mutableStateOf(true)
        private set

    var isAgentThinking by mutableStateOf(false)
        private set

    var gameState by mutableStateOf<GameState>(GameState.Continue)
        private set

    // Statistiche
    var gamesPlayed by mutableStateOf(0)
        private set
    var gamesWon by mutableStateOf(0)
        private set
    var gamesLost by mutableStateOf(0)
        private set
    var gamesDrawn by mutableStateOf(0)
        private set

    private var currentPlayer by mutableStateOf(GameConfig.USER_PLAYER)

    fun mymove(row: Int, col: Int) {
        if (board[row][col].isEmpty() && currentPlayer == GameConfig.USER_PLAYER && gameState == GameState.Continue) {
            val newBoard = board.toMutableList().map { it.toMutableList() }
            newBoard[row][col] = GameConfig.USER_PLAYER
            board = newBoard.map { it.toList() }

            postUserTurn()
            checkGameStatus()

            if (gameState == GameState.Continue) {
                agentMove()
            }
        }
    }

    private fun agentMove() {
        viewModelScope.launch {
            isBoardEnabled = false
            isAgentThinking = true
            val move = getAgentMoveUseCase.getMove(board)
            isAgentThinking = false

            if (move != null) {
                val newBoard = board.toMutableList().map { it.toMutableList() }
                newBoard[move.first][move.second] = GameConfig.AGENT_PLAYER
                board = newBoard.map { it.toList() }
                checkGameStatus()
            }

            if (gameState == GameState.Continue) {
                currentPlayer = GameConfig.USER_PLAYER
                isBoardEnabled = true
            }
        }
    }

    private fun postUserTurn() {
        viewModelScope.launch {
            postTurnUseCase(board)
        }
    }

    private fun checkGameStatus() {
        val currentGameState = checkWinConditionUseCase.check(board)
        if (currentGameState != GameState.Continue) {
            gamesPlayed++
            when (currentGameState) {
                is GameState.Win -> {
                    if (currentGameState.winner == GameConfig.USER_PLAYER) gamesWon++ else gamesLost++
                }
                GameState.Draw -> gamesDrawn++
                else -> {}
            }
        }
        gameState = currentGameState
    }

    fun resetGame() {
        board = List(3) { List(3) { "" } }
        currentPlayer = GameConfig.USER_PLAYER
        gameState = GameState.Continue
        isBoardEnabled = true
        isAgentThinking = false

        viewModelScope.launch {
            resetGameUseCase()
        }
    }
}
