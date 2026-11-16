package com.example.spacedefender.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {

    @Insert
    suspend fun insert(score: ScoreEntity)

    @Query("SELECT * FROM scores ORDER BY date DESC")
    fun getAll(): Flow<List<ScoreEntity>>

    @Query("SELECT * FROM scores ORDER BY score DESC, date ASC LIMIT :limit")
    fun getTop(limit: Int): Flow<List<ScoreEntity>>
}
