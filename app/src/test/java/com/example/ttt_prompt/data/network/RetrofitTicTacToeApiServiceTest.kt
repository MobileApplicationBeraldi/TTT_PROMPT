package com.example.ttt_prompt.data.network

import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RetrofitTicTacToeApiServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var apiService: RetrofitTicTacToeApiService

    @Before
    fun setUp() {
        server = MockWebServer()
        apiService = RetrofitTicTacToeApiService(server.url("/").toString())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getMove returns a move when the api call is successful`() = runBlocking {
        val board = listOf(
            listOf("X", "", ""),
            listOf("", "O", ""),
            listOf("", "", "X")
        )
        val responseJson = Gson().toJson(MoveResponse(listOf(0, 1)))
        server.enqueue(MockResponse().setBody(responseJson))

        val move = apiService.getMove(board)

        assertEquals(Pair(0, 1), move)
    }
}
