package com.example.spacedefender

import com.example.spacedefender.data.GameRepository
import com.example.spacedefender.data.ScoreDao
import com.example.spacedefender.data.ScoreEntity
import com.example.spacedefender.game.GameEngine
import com.example.spacedefender.model.GameStatus
import com.example.spacedefender.model.Meteor
import com.example.spacedefender.viewmodel.GameViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/** DAO falso em memória, para testar o ViewModel sem Room/Android. */
private class FakeScoreDao : ScoreDao {
    val inserted = mutableListOf<ScoreEntity>()

    override suspend fun insert(score: ScoreEntity) {
        inserted.add(score)
    }

    override fun getAll(): Flow<List<ScoreEntity>> = flowOf(inserted.toList())

    override fun getTop(limit: Int): Flow<List<ScoreEntity>> =
        flowOf(inserted.sortedByDescending { it.score }.take(limit))
}

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private lateinit var dao: FakeScoreDao
    private lateinit var engine: GameEngine
    private lateinit var viewModel: GameViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        dao = FakeScoreDao()
        engine = GameEngine(Random(1))
        viewModel = GameViewModel(GameRepository(dao), engine)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** Coloca um meteoro parado exatamente em cima da nave. */
    private fun meteoroNaNave() {
        engine.meteors.clear()
        engine.meteors.add(Meteor(engine.spaceship.x, engine.spaceship.y, 40f, 0f))
    }

    @Test
    fun estadoInicial_estaPronto() {
        val state = viewModel.state.value
        assertEquals(GameStatus.READY, state.status)
        assertEquals(3, state.lives)
        assertEquals(0, state.score)
    }

    @Test
    fun startGame_iniciaPartida() {
        viewModel.startGame()
        val state = viewModel.state.value
        assertEquals(GameStatus.RUNNING, state.status)
        assertEquals(3, state.lives)
        assertEquals(0, state.score)
    }

    @Test
    fun inclinarParaDireita_movePosicaoDaNave() {
        viewModel.startGame()
        val xInicial = viewModel.state.value.ship.x

        viewModel.onTilt(6f)
        viewModel.tick(0.1f)

        assertTrue(viewModel.state.value.ship.x > xInicial)
    }

    @Test
    fun inclinarParaEsquerda_movePosicaoDaNave() {
        viewModel.startGame()
        val xInicial = viewModel.state.value.ship.x

        viewModel.onTilt(-6f)
        viewModel.tick(0.1f)

        assertTrue(viewModel.state.value.ship.x < xInicial)
    }

    @Test
    fun nave_naoUltrapassaAsBordas() {
        viewModel.startGame()
        viewModel.onTilt(6f)
        repeat(100) { viewModel.tick(0.1f) }

        val ship = viewModel.state.value.ship
        assertTrue(ship.x <= engine.worldWidth - ship.width / 2f)
    }

    @Test
    fun colisaoComMeteoro_reduzUmaVida() {
        viewModel.startGame()
        meteoroNaNave()

        viewModel.tick(0.016f)

        assertEquals(2, viewModel.state.value.lives)
        assertEquals(GameStatus.RUNNING, viewModel.state.value.status)
    }

    @Test
    fun semVidas_partidaEncerraESalvaPontuacao() {
        viewModel.startGame()

        repeat(3) {
            meteoroNaNave()
            viewModel.tick(2f) // 2s > tempo de invulnerabilidade, permite novo dano
        }

        assertEquals(GameStatus.GAME_OVER, viewModel.state.value.status)
        assertEquals(0, viewModel.state.value.lives)
        assertEquals(1, dao.inserted.size)
    }

    @Test
    fun aposGameOver_naoSalvaDuasVezes() {
        viewModel.startGame()
        repeat(3) {
            meteoroNaNave()
            viewModel.tick(2f)
        }
        repeat(5) { viewModel.tick(0.016f) }

        assertEquals(1, dao.inserted.size)
    }

    @Test
    fun pausado_naoAtualizaOJogo() {
        viewModel.startGame()
        viewModel.onTilt(6f)
        viewModel.pauseGame()
        val xAntes = viewModel.state.value.ship.x

        viewModel.tick(0.5f)

        assertEquals(GameStatus.PAUSED, viewModel.state.value.status)
        assertEquals(xAntes, viewModel.state.value.ship.x, 0.001f)
    }

    @Test
    fun segurarATela_disparaProjeteis() {
        viewModel.startGame()
        viewModel.setFiring(true)

        viewModel.tick(0.016f)

        assertTrue(viewModel.state.value.bullets.isNotEmpty())
    }
}
