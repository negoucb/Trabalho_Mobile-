package com.example.spacedefender

import com.example.spacedefender.game.GameEngine
import com.example.spacedefender.model.Bullet
import com.example.spacedefender.model.Meteor
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class ScoreTest {

    private fun newEngine() = GameEngine(Random(1)).also { it.start() }

    @Test
    fun meteoroPequeno_vale10Pontos() {
        assertEquals(10, GameEngine.pointsFor(Meteor(0f, 0f, 40f, 0f)))
    }

    @Test
    fun meteoroGrande_vale20Pontos() {
        assertEquals(20, GameEngine.pointsFor(Meteor(0f, 0f, 65f, 0f)))
    }

    @Test
    fun pontuacaoComecaEmZero() {
        assertEquals(0, newEngine().score)
    }

    @Test
    fun destruirMeteoro_somaPontos() {
        val engine = newEngine()
        engine.meteors.add(Meteor(x = 500f, y = 300f, radius = 60f, speed = 0f))
        engine.bullets.add(Bullet(x = 500f, y = 300f))

        engine.update(0.016f)

        assertEquals(20, engine.score)
        assertEquals(0, engine.meteors.size)
        assertEquals(0, engine.bullets.size)
    }

    @Test
    fun pontuacaoAcumulaEmVariosAcertos() {
        val engine = newEngine()
        engine.meteors.add(Meteor(x = 300f, y = 300f, radius = 40f, speed = 0f))
        engine.bullets.add(Bullet(x = 300f, y = 300f))
        engine.update(0.016f)

        engine.meteors.add(Meteor(x = 700f, y = 300f, radius = 40f, speed = 0f))
        engine.bullets.add(Bullet(x = 700f, y = 300f))
        engine.update(0.016f)

        assertEquals(20, engine.score)
    }
}
