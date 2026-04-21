package domain.engine

import domain.models.BoardState
import domain.models.MoveRequest
import domain.models.ScoreCategory
import domain.models.ScoreEvent

class YahtzeeRulesEngine: IRulesEngine {
    override fun validateMove(board: BoardState, move: MoveRequest) : Boolean
    {
        return (move.playerID in board.players && board.playerSheets[move.playerID]?.filledCategories == null)
    }

    private fun isNOfAKind(dice: List<Int>, num: Int) : Boolean
    {
        return dice.groupBy { it }.values.any{it.size >= num}
    }

    private fun isFullHouse(dice: List<Int>) : Boolean
    {
        val counter = dice.groupingBy { it }.eachCount()
        val counts = counter.values.sorted()
        if (counts.size == 2)
        {
            return counts == listOf(2,3)
        }
        else if (counts.size == 1)
        {
            return counts == listOf(5)
        }
        return false
    }

    private fun isAnyStraight(dice: List<Int>, num: Int) : Boolean
    {
        var sortedLen = 1

        for (i in 1 until dice.size)
        {
            if (dice[i] >= dice[i-1])
            {
                sortedLen++
            }
            else
            {
                sortedLen = 1
            }

            if (sortedLen == num)
            {
                return true
            }
        }

        return false

    }

    private fun isYahtzee(dice: List<Int>) : Boolean
    {
        return dice.groupingBy { it }.eachCount().values.size == 1
    }

    override fun calculateIntermediateScore(move: MoveRequest): ScoreEvent
    {
        if (move.finalDice.size != 5)
            throw NoSuchElementException("Должно быть передано 5 кубиков")

        val points = when (move.targetCategory)
        {
            // UPPER PART
            ScoreCategory.ONES -> move.finalDice.filter {it == 1}.sum()
            ScoreCategory.TWOS -> move.finalDice.filter {it == 2}.sum()
            ScoreCategory.THREES -> move.finalDice.filter {it == 3}.sum()
            ScoreCategory.FOURTHS -> move.finalDice.filter {it == 4}.sum()
            ScoreCategory.FIFTHS -> move.finalDice.filter {it == 5}.sum()
            ScoreCategory.SIXS -> move.finalDice.filter {it == 6}.sum()

            // LOWER PART
            ScoreCategory.THREEKIND -> if (isNOfAKind(move.finalDice,3)) move.finalDice.sum() else 0
            ScoreCategory.FOURKIND -> if (isNOfAKind(move.finalDice,4)) move.finalDice.sum() else 0
            ScoreCategory.FULL_HOUSE -> if (isFullHouse(move.finalDice)) 25 else 0
            ScoreCategory.SMALL_STRAIGHT -> if (isAnyStraight(move.finalDice,4)) 30 else 0
            ScoreCategory.LARGE_STRAIGHT -> if (isAnyStraight(move.finalDice,5)) 40 else 0
            ScoreCategory.YAHTZEE -> if (isYahtzee(move.finalDice)) 50 else 0
            ScoreCategory.CHANCE -> move.finalDice.sum()
        }
        return ScoreEvent(move.playerID,
            points = points,
            category = move.targetCategory,
            isBonusApplied = false)
    }

    override fun calculateFinalScore(board: BoardState): List<ScoreEvent> {
        val bonusEvent = mutableListOf<ScoreEvent>()
        val upperCategories = listOf<Int>(1,2,3,4,5,6)

        for (player in board.players)
        {
            val sheet = board.playerSheets[player]
            var upperPartScore = 0
            for (cat in upperCategories)
            {
                val categoryValue = sheet?.filledCategories?.get(cat)
                upperPartScore += categoryValue ?: 0
            }

        }
        }
    }
}