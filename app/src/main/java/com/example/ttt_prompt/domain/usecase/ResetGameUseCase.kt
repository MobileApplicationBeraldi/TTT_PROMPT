package com.example.ttt_prompt.domain.usecase

import com.example.ttt_prompt.data.network.RetrofitTicTacToeApiService

interface ResetGameUseCase {
    suspend operator fun invoke()
}

class ResetGameUseCaseImpl(
    private val apiService: TicTacToeApiService = RetrofitTicTacToeApiService()
) : ResetGameUseCase {
    override suspend fun invoke() {
        apiService.reset()
    }
}
