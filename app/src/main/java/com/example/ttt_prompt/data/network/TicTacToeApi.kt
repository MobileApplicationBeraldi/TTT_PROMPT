package com.example.ttt_prompt.data.network

import retrofit2.http.Body
import retrofit2.http.POST

interface TicTacToeApi {
    @POST("move")
    suspend fun getMove(@Body moveRequest: MoveRequest): MoveResponse
}
