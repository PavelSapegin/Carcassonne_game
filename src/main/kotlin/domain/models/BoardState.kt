package domain.models

import java.util.UUID

class BoardState(val players: List<UUID>) {
    var playerSheets: MutableMap<UUID, ScoreSheet> = players.associateWith { ScoreSheet() } as MutableMap<UUID, ScoreSheet>

    fun applyMove(move: MoveRequest, points: Int)
    {
        playerSheets[move.playerID]?.filledCategories[move.targetCategory] = points
    }

    fun revertMove(record: MoveRecord)
    {
        playerSheets[record.requestData.playerID]?.filledCategories?.remove(record.requestData.targetCategory)
    }
}