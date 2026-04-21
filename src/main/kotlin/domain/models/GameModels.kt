package domain.models

import java.sql.Timestamp
import java.util.UUID


data class ScoreEvent(
    val playerID: UUID,
    val points: Int,
    val category: ScoreCategory,
    val isBonusApplied: Boolean
)

data class MoveRequest(
    val playerID: UUID,
    val finalDice: List<Int>,
    val targetCategory: ScoreCategory
)

data class MoveRecord(
    val moveNumber: Int,
    val requestData: MoveRequest,
    val timestamp: Timestamp,
    val pointScored: Int
)

data class ScoreSheet(
    val filledCategories: MutableMap<ScoreCategory,Int> = mutableMapOf()
)