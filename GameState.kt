package com.example.spacedefender.model

enum class GameStatus { READY, RUNNING, PAUSED, GAME_OVER }

/**
 * Retrato imutável do estado da partida, publicado pelo ViewModel para a View.
 */
data class GameState(
    val score: Int = 0,
    val lives: Int = 3,
    val status: GameStatus = GameStatus.READY,
    val ship: Spaceship,
    val meteors: List<Meteor> = emptyList(),
    val bullets: List<Bullet> = emptyList(),
    val worldWidth: Float,
    val worldHeight: Float,
    val invulnerable: Boolean = false
)
