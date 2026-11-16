package com.example.spacedefender.game

import com.example.spacedefender.model.Bullet
import com.example.spacedefender.model.GameState
import com.example.spacedefender.model.GameStatus
import com.example.spacedefender.model.Meteor
import com.example.spacedefender.model.Spaceship
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

/**
 * Motor do jogo: Kotlin puro (sem dependência de Android), por isso é fácil de testar.
 * Atualiza posições, disparos, meteoros, colisões, vidas e pontuação.
 */
class GameEngine(
    random: Random = Random.Default,
    worldWidth: Float = DEFAULT_WORLD_WIDTH,
    worldHeight: Float = DEFAULT_WORLD_HEIGHT
) {

    companion object {
        const val DEFAULT_WORLD_WIDTH = 1000f
        const val DEFAULT_WORLD_HEIGHT = 1800f
        const val INITIAL_LIVES = 3

        const val POINTS_SMALL = 10
        const val POINTS_LARGE = 20
        const val LARGE_METEOR_RADIUS = 55f

        const val MAX_SHIP_SPEED = 800f      // unidades do mundo por segundo
        const val TILT_DEADZONE = 0.4f       // m/s² ignorados (evita tremor)
        const val TILT_MAX = 6f              // m/s² para atingir a velocidade máxima
        const val FIRE_COOLDOWN = 0.28f      // segundos entre disparos
        const val INVULNERABLE_TIME = 1.5f   // segundos após levar dano
        const val SHIP_BOTTOM_MARGIN = 200f

        /** Meteoros grandes valem mais pontos. */
        fun pointsFor(meteor: Meteor): Int =
            if (meteor.radius >= LARGE_METEOR_RADIUS) POINTS_LARGE else POINTS_SMALL
    }

    var worldWidth: Float = worldWidth
        private set
    var worldHeight: Float = worldHeight
        private set

    val spaceship = Spaceship(x = worldWidth / 2f, y = worldHeight - SHIP_BOTTOM_MARGIN)
    val meteors = mutableListOf<Meteor>()
    val bullets = mutableListOf<Bullet>()

    var score: Int = 0
        private set
    var lives: Int = INITIAL_LIVES
        private set
    var status: GameStatus = GameStatus.READY
        private set
    var difficulty: Float = 1f
        private set

    /** Inclinação lateral lida do acelerômetro (positivo = direita). */
    @Volatile
    var tiltX: Float = 0f

    /** True enquanto o jogador mantém o dedo na tela. */
    @Volatile
    var firing: Boolean = false

    private val spawner = MeteorSpawner(random)
    private val collisions = CollisionManager()

    private var elapsed = 0f
    private var fireTimer = 0f
    private var invulnerableTimer = 0f

    // ---------- Controle da partida ----------

    fun start(difficulty: Float = 1f) {
        this.difficulty = difficulty.coerceAtLeast(0.5f)
        score = 0
        lives = INITIAL_LIVES
        elapsed = 0f
        fireTimer = 0f
        invulnerableTimer = 0f
        meteors.clear()
        bullets.clear()
        spawner.reset()
        spaceship.x = worldWidth / 2f
        spaceship.y = worldHeight - SHIP_BOTTOM_MARGIN
        status = GameStatus.RUNNING
    }

    fun pause() {
        if (status == GameStatus.RUNNING) status = GameStatus.PAUSED
    }

    fun resume() {
        if (status == GameStatus.PAUSED) status = GameStatus.RUNNING
    }

    fun setWorldSize(width: Float, height: Float) {
        worldWidth = width
        worldHeight = height
        spaceship.y = height - SHIP_BOTTOM_MARGIN
        spaceship.x = spaceship.x.coerceIn(spaceship.width / 2f, width - spaceship.width / 2f)
    }

    // ---------- Atualização (um frame) ----------

    fun update(dt: Float) {
        if (status != GameStatus.RUNNING) return

        elapsed += dt
        if (invulnerableTimer > 0f) invulnerableTimer = max(0f, invulnerableTimer - dt)

        moveSpaceship(dt)
        handleFiring(dt)

        spawner.update(dt, elapsed, worldWidth, difficulty)?.let { meteors.add(it) }

        for (m in meteors) m.y += m.speed * dt
        for (b in bullets) b.y -= b.speed * dt

        resolveBulletHits()
        resolveShipHits()
        removeDeadAndOffscreen()
    }

    /** Converte a inclinação (m/s²) em velocidade horizontal, com zona morta. */
    fun velocityFromTilt(tilt: Float): Float {
        if (abs(tilt) < TILT_DEADZONE) return 0f
        return (tilt / TILT_MAX).coerceIn(-1f, 1f) * MAX_SHIP_SPEED
    }

    fun moveSpaceship(dt: Float) {
        val newX = spaceship.x + velocityFromTilt(tiltX) * dt
        // Limites horizontais: a nave não passa das bordas da tela
        spaceship.x = newX.coerceIn(spaceship.width / 2f, worldWidth - spaceship.width / 2f)
    }

    private fun handleFiring(dt: Float) {
        fireTimer = max(0f, fireTimer - dt)
        if (firing && fireTimer <= 0f) {
            bullets.add(Bullet(x = spaceship.x, y = spaceship.y - spaceship.height / 2f))
            fireTimer = FIRE_COOLDOWN
        }
    }

    private fun resolveBulletHits() {
        for (bullet in bullets) {
            if (!bullet.alive) continue
            for (meteor in meteors) {
                if (!meteor.alive) continue
                if (collisions.bulletHitsMeteor(bullet, meteor)) {
                    bullet.alive = false
                    meteor.alive = false
                    score += pointsFor(meteor)
                    break
                }
            }
        }
    }

    private fun resolveShipHits() {
        if (invulnerableTimer > 0f) return
        for (meteor in meteors) {
            if (!meteor.alive) continue
            if (collisions.shipHitsMeteor(spaceship, meteor)) {
                meteor.alive = false
                lives -= 1
                invulnerableTimer = INVULNERABLE_TIME
                if (lives <= 0) {
                    lives = 0
                    status = GameStatus.GAME_OVER
                }
                break
            }
        }
    }

    private fun removeDeadAndOffscreen() {
        meteors.removeAll { !it.alive || it.y - it.radius > worldHeight }
        bullets.removeAll { !it.alive || it.y + it.radius < 0f }
    }

    // ---------- Estado para a interface ----------

    fun snapshot(): GameState = GameState(
        score = score,
        lives = lives,
        status = status,
        ship = spaceship.copy(),
        meteors = meteors.map { it.copy() },
        bullets = bullets.map { it.copy() },
        worldWidth = worldWidth,
        worldHeight = worldHeight,
        invulnerable = invulnerableTimer > 0f
    )
}
