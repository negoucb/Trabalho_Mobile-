package com.example.spacedefender.game

import com.example.spacedefender.model.Meteor
import kotlin.random.Random

/**
 * Cria meteoros em posições horizontais aleatórias.
 * Quanto mais tempo de partida (e maior a dificuldade da missão), mais rápido e mais frequente.
 */
class MeteorSpawner(private val random: Random = Random.Default) {

    private var timer = 0f

    fun reset() {
        timer = 0f
    }

    /** Retorna um novo meteoro quando o intervalo de spawn foi atingido, ou null. */
    fun update(dt: Float, elapsed: Float, worldWidth: Float, difficulty: Float): Meteor? {
        timer += dt
        if (timer < spawnInterval(elapsed, difficulty)) return null
        timer = 0f
        return createMeteor(elapsed, worldWidth, difficulty)
    }

    fun spawnInterval(elapsed: Float, difficulty: Float): Float =
        (1.1f - elapsed * 0.012f).coerceAtLeast(0.4f) / difficulty

    fun createMeteor(elapsed: Float, worldWidth: Float, difficulty: Float): Meteor {
        val radius = MIN_RADIUS + random.nextFloat() * (MAX_RADIUS - MIN_RADIUS)
        val x = radius + random.nextFloat() * (worldWidth - 2f * radius)
        val baseSpeed = 200f + random.nextFloat() * 150f + elapsed * 4f
        val speed = baseSpeed.coerceAtMost(650f) * difficulty
        return Meteor(x = x, y = -radius, radius = radius, speed = speed)
    }

    companion object {
        const val MIN_RADIUS = 30f
        const val MAX_RADIUS = 70f
    }
}
