package application.services

import application.models.SessionState
import data.repositories.IGameRepository
import domain.engine.IRulesEngine
import domain.models.MoveRecord

class GameSessionManager(
    val referee: IRulesEngine,
    val gameRepo: IGameRepository,
    var currentState: SessionState,
    var moveHistory: MutableList<MoveRecord>
    ) {
}