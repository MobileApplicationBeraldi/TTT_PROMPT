package com.example.ttt_prompt.data.network

import android.util.Log
import com.example.ttt_prompt.GameConfig
import com.example.ttt_prompt.domain.usecase.Board
import com.example.ttt_prompt.domain.usecase.TicTacToeApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitTicTacToeApiService(baseUrl: String = GameConfig.BASE_URL) : TicTacToeApiService {
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(TicTacToeApi::class.java)

    override suspend fun getMove(board: Board): Pair<Int, Int>? {
        Log.d("ApiService", "Sending board to server: $board")
        return try {
            val response = api.getMove(MoveRequest(board))
            Log.d("ApiService", "Received move from server: ${response.move}")
            Pair(response.move[0], response.move[1])
        } catch (e: Exception) {
            Log.e("ApiService", "Error calling getMove API", e)
            null
        }
    }

    override suspend fun postTurn(board: Board) {
        Log.d("ApiService", "Posting user turn to server: $board")
        try {
            api.postTurn(MoveRequest(board))
            Log.d("ApiService", "Successfully posted turn.")
        } catch (e: Exception) {
            Log.e("ApiService", "Error calling postTurn API", e)
        }
    }

    override suspend fun reset() {
        Log.d("ApiService", "Notifying server of game reset.")
        try {
            api.reset()
            Log.d("ApiService", "Successfully notified reset.")
        } catch (e: Exception) {
            Log.e("ApiService", "Error calling reset API", e)
        }
    }
}
