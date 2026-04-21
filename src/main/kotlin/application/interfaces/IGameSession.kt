package application.interfaces

import application.models.MoveResult
import application.models.SessionState
import domain.models.MoveRecord
import domain.models.MoveRequest
import java.util.UUID

interface IGameSession {
    fun startGame(playerIds: List<UUID>)
    fun registerMove(move: MoveRequest) : MoveResult
    fun undoLastMove(move: MoveRecord)
    fun endGame() : GameRecord
    fun getCurrentState() : SessionState
}