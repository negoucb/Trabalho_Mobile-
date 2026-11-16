package com.example.spacedefender.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scores")
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val score: Int,
    /** Data/hora da partida em milissegundos (epoch). */
    val date: Long = System.currentTimeMillis()
)
