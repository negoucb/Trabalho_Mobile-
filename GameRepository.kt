package com.example.spacedefender.data

import kotlinx.coroutines.flow.Flow

/** Intermedia o acesso aos dados: o ViewModel conversa com o Repository, e este com o DAO. */
class GameRepository(private val dao: ScoreDao) {

    fun topScores(limit: Int = 5): Flow<List<ScoreEntity>> = dao.getTop(limit)

    fun allScores(): Flow<List<ScoreEntity>> = dao.getAll()

    suspend fun saveScore(score: Int) {
        dao.insert(ScoreEntity(score = score))
    }
}
