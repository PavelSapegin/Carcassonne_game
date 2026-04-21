import application.models.GameStatus
import application.models.MoveResult
import application.services.GameSessionManager
import data.models.PlayerProfile
import data.repositories.InMemoryGameRepository
import data.repositories.InMemoryPlayerRepository
import data.repositories.InMemoryStatsManager
import domain.engine.YahtzeeRulesEngine
import domain.models.MoveRequest
import domain.models.ScoreCategory
import java.util.UUID
import java.util.UUID.randomUUID

fun main() {
    val playerRepo = InMemoryPlayerRepository()
    val gameRepo = InMemoryGameRepository()
    val statsManager = InMemoryStatsManager(playerRepo)

    val referee = YahtzeeRulesEngine()
    val manager = GameSessionManager(referee, gameRepo)

    val players = mutableMapOf<UUID, String>()

    while (true) {
        println("Please, write name of new player or write exit for the ending.")
        val name = readln()
        if (name != "exit") {
            val id = randomUUID()
            playerRepo.save(PlayerProfile(id, name, 1000, 0, 0F))
            players[id] = name
        } else {
            break
        }
    }

    manager.startGame(players.keys.toList())
    println("Attention! If you want to cancel wrong move, please write cancel instead of dice")
    while (manager.currentState.status == GameStatus.IN_PROGRESS) {
        val state = manager.currentState

        println("Current move: ${players[state.currentPlayerId]}. Please, write a final dice with splitting by space:")
        val input = readln()
        if (input == "cancel") {
            manager.undoLastMove()
            continue
        }
        val dice = input.split(" ").map { it.toInt() }

        println("Please, write a target category:")
        val cat = ScoreCategory.valueOf(readln().uppercase())

        val result = manager.registerMove(MoveRequest(state.currentPlayerId, dice, cat))
        if (result is MoveResult.Error) println("Error: ${result.errorMessage}")
    }

    val record = manager.endGame()
    println("Game was ended.")
}
