package com.example.ttt_prompt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ttt_prompt.domain.usecase.GameState
import com.example.ttt_prompt.ui.theme.TTT_PROMPTTheme

class MainActivity : ComponentActivity() {
    private val viewModel: TicTacToeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TTT_PROMPTTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TicTacToeScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun TicTacToeScreen(viewModel: TicTacToeViewModel) {
    val board = viewModel.board
    val gameState = viewModel.gameState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Griglia di gioco
        Box(contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                board.forEachIndexed { rowIndex, row ->
                    Row {
                        row.forEachIndexed { colIndex, cell ->
                            Button(
                                onClick = { viewModel.mymove(rowIndex, colIndex) },
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(100.dp)
                                    .testTag("cell_${rowIndex}_${colIndex}"),
                                shape = RectangleShape,
                                enabled = viewModel.isBoardEnabled
                            ) {
                                Text(text = cell, style = MaterialTheme.typography.headlineLarge)
                            }
                        }
                    }
                }
            }

            if (viewModel.isAgentThinking) {
                CircularProgressIndicator(modifier = Modifier.testTag("agent_thinking_indicator"))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Sezione Statistiche
        GameStats(viewModel)

        // Overlay di fine partita
        if (gameState != GameState.Continue) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { viewModel.resetGame() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val message = when (gameState) {
                        is GameState.Win -> "Winner: ${(gameState as GameState.Win).winner}"
                        GameState.Draw -> "Draw"
                        else -> ""
                    }
                    Text(text = message, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Text(text = "Click to continue", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun GameStats(viewModel: TicTacToeViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("STATISTICHE", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = "Giocate: ${viewModel.gamesPlayed}")
            Text(text = "Vinte: ${viewModel.gamesWon}")
            Text(text = "Perse: ${viewModel.gamesLost}")
            Text(text = "Pari: ${viewModel.gamesDrawn}")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TicTacToeScreenPreview() {
    TTT_PROMPTTheme {
        TicTacToeScreen(viewModel = TicTacToeViewModel())
    }
}
