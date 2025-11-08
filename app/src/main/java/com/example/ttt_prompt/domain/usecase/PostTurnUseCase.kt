package com.example.ttt_prompt.domain.usecase

import com.example.ttt_prompt.data.network.RetrofitTicTacToeApiService

interface PostTurnUseCase {
    suspend operator fun invoke(board: Board)
}

class PostTurnUseCaseImpl(
    private val apiService: TicTacToeApiService = RetrofitTicTacToeApiService()
) : PostTurnUseCase {
    override suspend fun invoke(board: Board) {
        apiService.postTurn(board)
    }
}
