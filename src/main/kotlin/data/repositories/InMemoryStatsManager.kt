package data.repositories

import data.models.GameRecord
import data.models.PlayerProfile
import java.util.UUID

class InMemoryStatsService : IStatsService {

    override fun processGameResult(record: GameRecord) {
        TODO("Not yet implemented")
    }

    override fun getPlayerStats(playerId: UUID): PlayerProfile {
        TODO("Not yet implemented")
    }

    override fun getLeaderBoard(): List<PlayerProfile> {
        TODO("Not yet implemented")
    }
}