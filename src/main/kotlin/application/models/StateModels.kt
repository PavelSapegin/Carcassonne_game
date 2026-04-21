package application.models

import java.util.UUID

data class PlayerInGameState(
    var currentScore: Int
)

enum class GameStatus
{
    PREPARING,
    IN_PROGRESS,
    FINISHED
}

data class SessionState(
    val gameId: UUID,
    val status: GameStatus,
    var currentPlayerId: UUID,
    val turnOrder: List<UUID>,
    var players: MutableMap<UUID, PlayerInGameState>
)

sealed class MoveResult(
    val isSuccess: Boolean,
    val errorMessage: String?
)
