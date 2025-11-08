package com.example.ttt_prompt.domain.usecase

sealed class GameState {
    object Continue : GameState()
    data class Win(val winner: String) : GameState()
    object Draw : GameState()
}

interface CheckWinConditionUseCase {
    fun check(board: Board): GameState
}

class CheckWinConditionUseCaseImpl : CheckWinConditionUseCase {
    override fun check(board: Board): GameState {
        // Check rows
        for (i in 0..2) {
            if (board[i][0].isNotEmpty() && board[i][0] == board[i][1] && board[i][0] == board[i][2]) {
                return GameState.Win(board[i][0])
            }
        }

        // Check columns
        for (i in 0..2) {
            if (board[0][i].isNotEmpty() && board[0][i] == board[1][i] && board[0][i] == board[2][i]) {
                return GameState.Win(board[0][i])
            }
        }

        // Check diagonals
        if (board[0][0].isNotEmpty() && board[0][0] == board[1][1] && board[0][0] == board[2][2]) {
            return GameState.Win(board[0][0])
        }
        if (board[0][2].isNotEmpty() && board[0][2] == board[1][1] && board[0][2] == board[2][0]) {
            return GameState.Win(board[0][2])
        }

        // Check for draw
        if (board.all { row -> row.all { it.isNotEmpty() } }) {
            return GameState.Draw
        }

        return GameState.Continue
    }
}
