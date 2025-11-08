package com.example.ttt_prompt.domain.usecase

import com.example.ttt_prompt.data.network.RetrofitTicTacToeApiService

typealias Board = List<List<String>>

interface TicTacToeApiService {
    suspend fun getMove(board: Board): Pair<Int, Int>?
}

interface GetAgentMoveUseCase {
    suspend fun getMove(board: Board): Pair<Int, Int>?
}

class GetRemoteAgentMoveUseCase(private val apiService: TicTacToeApiService = RetrofitTicTacToeApiService()) : GetAgentMoveUseCase {
    override suspend fun getMove(board: Board): Pair<Int, Int>? {

        return apiService.getMove(board)
    }
}
