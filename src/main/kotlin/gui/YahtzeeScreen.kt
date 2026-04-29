package gui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import domain.models.ScoreCategory
import java.lang.Exception

// Main UI component that renders different screens based on the current state
@Composable
fun yahtzeeAppScreen(viewModel: YahtzeeViewModel) {
    val state by viewModel.uiState.collectAsState()

    // Render different screens based on the current screen state
    when (state.currentScreen) {
        AppScreen.SETUP -> setupScreen(state, viewModel)
        AppScreen.GAME -> gameScreen(state, viewModel)
        AppScreen.LEADERBOARD -> leaderBoardTable(state)
    }
}

// Composable function for the setup screen
@Composable
fun setupScreen(
    state: UIState,
    viewModel: YahtzeeViewModel,
) {
    var nameInput by remember { mutableStateOf("") }

    val onAddPlayer = {
        viewModel.addPlayer(nameInput)
        nameInput = ""
    }
    // UI layout for player registration and game start
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Player registration", style = MaterialTheme.typography.h4)
        Spacer(Modifier.height(16.dp))

        Row {
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Player Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onAddPlayer() }),
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                viewModel.addPlayer(nameInput)
                nameInput = ""
            }, modifier = Modifier.height(56.dp)) {
                Text("Add")
            }
        }

        if (state.errorText.isNotEmpty()) {
            Text(state.errorText, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(Modifier.height(16.dp))
        Text("Players:", fontWeight = FontWeight.Bold)
        state.pendingPlayers.values.forEach { Text("- $it") }

        Spacer(Modifier.height(24.dp))
        Button(onClick = { viewModel.startGame() }, colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)) {
            Text("START GAME", fontWeight = FontWeight.Bold)
        }
    }
}

// Composable function for the main game screen, including score board and dice input
@Composable
fun gameScreen(
    state: UIState,
    viewModel: YahtzeeViewModel,
) {
    var diceInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // HEADER
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = "Current move: ${state.currentPlayerName}",
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.Bold,
            )
        }

        if (state.errorText.isNotEmpty()) {
            Text(text = state.errorText, color = Color.Red, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        Spacer(Modifier.height(16.dp))

        // TABLE
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (state.scoreBoard.isNotEmpty()) {
                scoreBoardTable(state, viewModel, diceInput) { diceInput = "" }
            }
        }

        Spacer(Modifier.height(16.dp))

        // -FOOTER + INPUT
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFFF5F5F5)).padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left button
            Button(onClick = { viewModel.undoMove() }, colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFCDD2))) {
                Text("Undo move")
            }

            // Center input
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                visualDiceRow(diceInput) // Отрисовка кубиков!
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = diceInput,
                    onValueChange = { diceInput = it },
                    label = { Text("Input dice:") },
                    modifier = Modifier.width(200.dp),
                )
            }

            // Right button
            Button(onClick = { viewModel.endGame() }, colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)) {
                Text("End Game")
            }
        }
    }
}

// Composable functions for rendering dice and score board
@Composable
fun visualDiceRow(input: String) {
    val numbers =
        try {
            input.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.map { it.toInt() }
        } catch (e: Exception) {
            emptyList()
        }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in 0 until 5) {
            val num = numbers.getOrNull(i)
            diceFace(value = num)
        }
    }
}

// Composable function to render a single dice face based on its value
@Composable
fun diceFace(value: Int?) {
    Box(
        modifier =
            Modifier
                .size(50.dp)
                .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (value != null && value in 1..6) diceDots(value)
    }
}

// Composable function to render the dots on a dice face based on its value
@Composable
fun diceDots(value: Int) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Upper row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            dot(visible = value > 1)
            dot(visible = false)
            dot(visible = value > 3)
        }

        // Middle row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            dot(visible = value == 6)
            dot(visible = value in listOf(1, 3, 5))
            dot(visible = value == 6)
        }

        // Low row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            dot(visible = value > 3)
            dot(visible = false)
            dot(visible = value > 1)
        }
    }
}

// Composable function to render a single dot on the dice face, visibility based on the dice value
@Composable
fun dot(visible: Boolean) {
    Box(
        modifier =
            Modifier
                .size(10.dp)
                .background(if (visible) Color.Black else Color.Transparent, CircleShape),
    )
}

// Composable function to render the score board table with player scores and categories
@Composable
fun scoreBoardTable(
    state: UIState,
    viewModel: YahtzeeViewModel,
    currentDiceInput: String,
    onMoveSubmitted: () -> Unit,
) {
    val players = state.scoreBoard.keys.toList()

    LazyColumn(modifier = Modifier.fillMaxWidth().border(1.dp, Color.Gray)) {
        item {
            Row(modifier = Modifier.background(Color.LightGray).padding(8.dp)) {
                Text("Category", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                players.forEach { Text(it, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold) }
            }
        }

        items(ScoreCategory.entries) { category ->
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = Modifier.weight(1.5f), verticalAlignment = Alignment.CenterVertically) {
                    Text(category.name, modifier = Modifier.weight(1f))
                    if (category != ScoreCategory.BONUS) {
                        Button(
                            onClick = {
                                viewModel.submitMove(currentDiceInput, category)
                                onMoveSubmitted()
                            },
                            modifier = Modifier.height(30.dp),
                        ) {
                            Text("->")
                        }
                    }
                }
                players.forEach { playerName ->
                    Text(state.scoreBoard[playerName]?.get(category) ?: "-", modifier = Modifier.weight(1f))
                }
            }
            Divider()
        }
        item {
            Row(modifier = Modifier.background(Color(0xFFE0E0E0)).padding(8.dp)) {
                Text("TOTAL:", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                players.forEach { playerName ->
                    Text(state.totalScores[playerName]?.toString() ?: "0", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Composable function to render the final leaderboard screen with player rankings and stats
@Composable
fun leaderBoardTable(state: UIState) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("FINAL RATING", style = MaterialTheme.typography.h3, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        state.finalLeaderBoard.forEachIndexed { index, profile ->
            Text("${index + 1}. ${profile.name} | ELO: ${profile.eloRating} | WINS: ${profile.winRate}%", fontSize = 20.sp)
            Spacer(Modifier.height(8.dp))
        }
    }
}
