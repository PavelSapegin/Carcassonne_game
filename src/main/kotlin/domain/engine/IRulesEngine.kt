package domain.engine

import domain.models.BoardState
import domain.models.MoveRequest
import domain.models.ScoreEvent

interface IRulesEngine {
    fun validateMove(
        board: BoardState,
        move: MoveRequest,
    ): Boolean

    fun calculateIntermediateScore(
        board: BoardState,
        move: MoveRequest,
    ): ScoreEvent

    fun calculateFinalScore(board: BoardState): List<ScoreEvent>
}
