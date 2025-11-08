package com.example.ttt_prompt

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class AgentSimulationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun simulate100Games() {
        val numberOfGames = 100

        for (game in 1..numberOfGames) {
            println("--- Inizio Partita di Test N.$game ---")

            // Ciclo per una singola partita
            while (true) {
                // Semplice attesa per la stabilità dell'UI
                composeTestRule.waitForIdle()

                // Controlla se la partita è finita
                if (isGameOver()) {
                    println("Partita N.$game terminata. Clicco per resettare.")

                    composeTestRule.onRoot().performClick()
                    // Attendi che l'azione di reset sia completata
                    composeTestRule.waitForIdle()
                    break // Esce dal ciclo della partita e passa alla successiva
                }

                val board = getBoardStateFromUi()
                val availableMoves = getAvailableMoves(board)

                if (availableMoves.isNotEmpty()) {
                    val randomMove = availableMoves.random(Random(System.currentTimeMillis()))
                    println("Mossa utente casuale: $randomMove")

                    composeTestRule
                        .onNode(hasTestTag("cell_${randomMove.first}_${randomMove.second}"))
                        .performClick()
                } else {
                     // Se non ci sono mosse disponibili, esce per evitare un loop infinito
                     break
                }
            }
        }
    }

    private fun isGameOver(): Boolean {
        return composeTestRule
            .onAllNodes(hasText("Winner", substring = true) or hasText("Draw", substring = true))
            .fetchSemanticsNodes()
            .isNotEmpty()
    }

    private fun getAvailableMoves(board: List<List<String>>): List<Pair<Int, Int>> {
        val moves = mutableListOf<Pair<Int, Int>>()
        board.forEachIndexed { r, row ->
            row.forEachIndexed { c, cell ->
                if (cell.isEmpty()) {
                    moves.add(Pair(r, c))
                }
            }
        }
        return moves
    }
    
    private fun getBoardStateFromUi(): List<List<String>> {
        val board = mutableListOf<MutableList<String>>().apply {
            for (i in 0..2) add(mutableListOf("", "", ""))
        }

        for (row in 0..2) {
            for (col in 0..2) {
                val node = composeTestRule
                    .onNode(hasTestTag("cell_${row}_${col}"))
                    .fetchSemanticsNode()

                val textList = node.children.firstOrNull()?.config?.getOrNull(SemanticsProperties.Text)
                board[row][col] = textList?.firstOrNull()?.text ?: ""
            }
        }
        return board
    }
}
