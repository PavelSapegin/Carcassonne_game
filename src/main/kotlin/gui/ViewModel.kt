package gui

import application.interfaces.IGameSession
import application.models.GameStatus
import application.models.MoveResult
import data.models.PlayerProfile
import data.repositories.IPlayerRepository
import data.repositories.IStatsService
import domain.models.MoveRequest
import domain.models.ScoreCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import java.util.UUID.randomUUID

// Enum and data class for UI state management
enum class AppScreen { SETUP, GAME, LEADERBOARD }

data class UIState(
    val currentScreen: AppScreen = AppScreen.SETUP,
    val pendingPlayers: Map<UUID, String> = emptyMap(),
    val currentPlayerName: String = "",
    val errorText: String = "",
    val scoreBoard: Map<UUID, Map<ScoreCategory, String>> = emptyMap(),
    val totalScores: Map<UUID, Int> = emptyMap(),
    val finalLeaderBoard: List<PlayerProfile> = emptyList(),
)

// ViewModel for the Yahtzee assistant
class YahtzeeViewModel(
    private val gameSession: IGameSession,
    private val playerRepo: IPlayerRepository,
    private val statsManager: IStatsService,
) {
    private val _uiState = MutableStateFlow(UIState()) // Mutable state flow to hold the UI state
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    // Function to add a player to the pending players list
    fun addPlayer(name: String) {
        if (name.isBlank()) return
        val id = randomUUID()
        playerRepo.save(PlayerProfile(id, name, 1000, 0, 0f))

        val newPlayers = _uiState.value.pendingPlayers.toMutableMap()
        newPlayers[id] = name
        _uiState.value = _uiState.value.copy(pendingPlayers = newPlayers)
    }

    // Function to start the game with the pending players
    fun startGame() {
        val players = _uiState.value.pendingPlayers.keys.toList()
        if (players.isEmpty()) {
            showError("Add at least 1 player")
            return
        }

        gameSession.startGame(players)
        _uiState.value = _uiState.value.copy(currentScreen = AppScreen.GAME)
        clearError()
        syncGameState()
    }

    // Function to submit a move based on user input
    fun submitMove(
        diceUnput: String,
        cat: ScoreCategory,
    ) {
        val dice = parseDiceInput(diceUnput)
        if (dice == null) {
            showError("Invalid format! Write 5 numbers splitting by spaces.")
            return
        }

        val request =
            MoveRequest(gameSession.currentState.currentPlayerId, dice, cat)

        when (val result = gameSession.registerMove(request)) {
            is MoveResult.Success ->
                {
                    clearError()
                    syncGameState()
                }
            is MoveResult.Error ->
                {
                    showError(result.errorMessage ?: "Unknown error")
                }
        }
    }

    // Function to undo the last move
    fun undoMove() {
        try {
            gameSession.undoLastMove()
            clearError()
            syncGameState()
        } catch (e: Exception) {
            showError("Nothing to cancel!")
        }
    }

    // Function to end the game and process results
    fun endGame() {
        try {
            val record = gameSession.endGame()
            statsManager.processGameResult(record)

            _uiState.value =
                _uiState.value.copy(
                    currentScreen = AppScreen.LEADERBOARD,
                    finalLeaderBoard =
                        statsManager.getLeaderBoard().sortedByDescending { it.eloRating },
                )
            syncGameState()
        } catch (e: Exception) {
            showError(e.message ?: "Can't to finish game.")
        }
    }

    fun showLeaderBoard() {
        _uiState.value =
            _uiState.value.copy(
                currentScreen = AppScreen.LEADERBOARD,
            )
    }

    // Private helper function to synchronize the UI state with the current game state
    private fun syncGameState() {
        val state = gameSession.currentState
        val newScoreBoard = mutableMapOf<UUID, Map<ScoreCategory, String>>()
        val newTotals = mutableMapOf<UUID, Int>()
        val pMap = _uiState.value.pendingPlayers
        if (state.status != GameStatus.FINISHED) {
            for ((playerId, sheet) in gameSession.board.playerSheets) {
                val column = mutableMapOf<ScoreCategory, String>()

                for (cat in ScoreCategory.entries) {
                    val score = sheet.filledCategories[cat]
                    column[cat] = score?.toString() ?: "-"
                }

                newScoreBoard[playerId] = column
                newTotals[playerId] =
                    state.players[playerId]?.currentScore ?: 0
            }
        }

        _uiState.value =
            _uiState.value.copy(
                currentPlayerName = pMap[state.currentPlayerId] ?: "",
                scoreBoard = newScoreBoard,
                totalScores = newTotals,
            )
    }

    // Private helper function to show error messages in the UI
    private fun showError(msg: String) {
        _uiState.value = _uiState.value.copy(errorText = msg)
    }

    private fun clearError() {
        _uiState.value = _uiState.value.copy(errorText = "")
    }

    // Private helper function to parse dice input from the user
    private fun parseDiceInput(input: String): List<Int>? {
        return try {
            val dice = input.trim().split(Regex("\\s+")).map { it.toInt() }
            if (dice.size == 5 && dice.all { it in 1..6 }) {
                dice
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
