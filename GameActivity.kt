package com.example.spacedefender.view

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.spacedefender.R
import com.example.spacedefender.model.GameStatus
import com.example.spacedefender.sensor.AccelerometerManager
import com.example.spacedefender.viewmodel.GameViewModel
import com.example.spacedefender.viewmodel.GameViewModelFactory
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class GameActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DIFFICULTY = "extra_difficulty"
    }

    private val viewModel: GameViewModel by viewModels {
        GameViewModelFactory.from(applicationContext)
    }

    private val difficulty: Float by lazy { intent.getFloatExtra(EXTRA_DIFFICULTY, 1f) }

    private lateinit var gameView: GameView
    private lateinit var accelerometer: AccelerometerManager
    private var gameOverDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        gameView = findViewById(R.id.gameView)
        gameView.bind(viewModel)

        accelerometer = AccelerometerManager(this) { tilt -> viewModel.onTilt(tilt) }
        if (!accelerometer.isAvailable) {
            Toast.makeText(
                this,
                "Este aparelho não tem acelerômetro. Use um celular real ou os sensores virtuais do emulador.",
                Toast.LENGTH_LONG
            ).show()
        }

        if (viewModel.state.value.status == GameStatus.READY) {
            viewModel.startGame(difficulty)
        }

        // A View observa o estado do ViewModel; aqui só reagimos ao fim da partida
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state
                    .map { it.status }
                    .distinctUntilChanged()
                    .collect { status ->
                        if (status == GameStatus.GAME_OVER) showGameOver()
                    }
            }
        }
    }

    private fun showGameOver() {
        if (gameOverDialog?.isShowing == true) return
        val score = viewModel.state.value.score
        gameOverDialog = AlertDialog.Builder(this)
            .setTitle("Fim de jogo")
            .setMessage("Sua pontuação: $score pontos")
            .setCancelable(false)
            .setPositiveButton("Jogar novamente") { _, _ -> viewModel.startGame(difficulty) }
            .setNegativeButton("Menu") { _, _ -> finish() }
            .show()
    }

    override fun onResume() {
        super.onResume()
        accelerometer.start()
        if (viewModel.state.value.status == GameStatus.PAUSED) viewModel.resumeGame()
    }

    override fun onPause() {
        super.onPause()
        accelerometer.stop()
        viewModel.setFiring(false)
        viewModel.pauseGame()
    }

    override fun onDestroy() {
        gameOverDialog?.dismiss()
        super.onDestroy()
    }
}
