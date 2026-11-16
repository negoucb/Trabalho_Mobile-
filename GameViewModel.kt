package com.example.spacedefender.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.spacedefender.data.GameDatabase
import com.example.spacedefender.data.GameRepository
import com.example.spacedefender.data.ScoreEntity
import com.example.spacedefender.game.GameEngine
import com.example.spacedefender.model.GameState
import com.example.spacedefender.model.GameStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel da partida: conecta a interface (View) ao motor do jogo e ao banco (Repository).
 * A View só observa [state]; quem altera o estado é o ViewModel.
 */
class GameViewModel(
    private val repository: GameRepository,
    private val engine: GameEngine = GameEngine()
) : ViewModel() {

    private val _state = MutableStateFlow(engine.snapshot())
    val state: StateFlow<GameState> = _state.asStateFlow()

    /** Melhores pontuações, vindas do Room. */
    val topScores: Flow<List<ScoreEntity>> = repository.topScores(5)

    private var scoreSaved = false

    @Synchronized
    fun startGame(difficulty: Float = 1f) {
        scoreSaved = false
        engine.start(difficulty)
        publish()
    }

    @Synchronized
    fun pauseGame() {
        engine.pause()
        publish()
    }

    @Synchronized
    fun resumeGame() {
        engine.resume()
        publish()
    }

    @Synchronized
    fun setWorldSize(width: Float, height: Float) {
        engine.setWorldSize(width, height)
        publish()
    }

    /** Recebe a inclinação do acelerômetro (positivo = direita). */
    fun onTilt(x: Float) {
        engine.tiltX = x
    }

    /** True enquanto o jogador segura a tela. */
    fun setFiring(firing: Boolean) {
        engine.firing = firing
    }

    /** Chamado a cada frame pelo loop do jogo. */
    @Synchronized
    fun tick(deltaSeconds: Float) {
        if (engine.status != GameStatus.RUNNING) return
        engine.update(deltaSeconds)
        publish()
        if (engine.status == GameStatus.GAME_OVER) saveScore()
    }

    private fun saveScore() {
        if (scoreSaved) return
        scoreSaved = true
        val finalScore = engine.score
        viewModelScope.launch { repository.saveScore(finalScore) }
    }

    private fun publish() {
        _state.value = engine.snapshot()
    }
}

class GameViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            return GameViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
    }

    companion object {
        fun from(context: Context): GameViewModelFactory =
            GameViewModelFactory(
                GameRepository(GameDatabase.getInstance(context.applicationContext).scoreDao())
            )
    }
}
