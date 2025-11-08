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
    private val checkWinConditionUseCase: CheckWinConditionUseCase = CheckWinConditionUseCaseImpl()
) : ViewModel() {
    var board by mutableStateOf(List(3) { List(3) { "" } })
        private set

    var isBoardEnabled by mutableStateOf(true)
        private set

    var isAgentThinking by mutableStateOf(false)
        private set

    var gameState by mutableStateOf<GameState>(GameState.Continue)
        private set

    private var currentPlayer by mutableStateOf(GameConfig.USER_PLAYER)

    fun mymove(row: Int, col: Int) {
        if (board[row][col].isEmpty() && currentPlayer == GameConfig.USER_PLAYER && gameState == GameState.Continue) {
            updateBoardAndCheckWinner(row, col)
            if (gameState == GameState.Continue) {
                agentMove()
            }
        }
    }

    private fun agentMove() {
        viewModelScope.launch {
            val ciccio = 0
            isBoardEnabled = false
            isAgentThinking = true
            val move = getAgentMoveUseCase.getMove(board)
            isAgentThinking = false
            if (move != null) {
                updateBoardAndCheckWinner(move.first, move.second)
            }
            if (gameState == GameState.Continue) {
                isBoardEnabled = true
            }
        }
    }

    private fun updateBoardAndCheckWinner(row: Int, col: Int) {
        val newBoard = board.toMutableList().map { it.toMutableList() }
        newBoard[row][col] = currentPlayer
        board = newBoard.map { it.toList() }
        currentPlayer = if (currentPlayer == GameConfig.USER_PLAYER) GameConfig.AGENT_PLAYER else GameConfig.USER_PLAYER
        gameState = checkWinConditionUseCase.check(board)
    }

    fun resetGame() {
        board = List(3) { List(3) { "" } }
        currentPlayer = GameConfig.USER_PLAYER
        gameState = GameState.Continue
        isBoardEnabled = true
        isAgentThinking = false
    }
}
