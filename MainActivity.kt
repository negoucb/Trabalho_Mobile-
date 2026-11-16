package com.example.spacedefender.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.spacedefender.R
import com.example.spacedefender.data.ScoreEntity
import com.example.spacedefender.viewmodel.GameViewModel
import com.example.spacedefender.viewmodel.GameViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val viewModel: GameViewModel by viewModels {
        GameViewModelFactory.from(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvRecords = findViewById<TextView>(R.id.tvRecords)

        findViewById<Button>(R.id.btnPlay).setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }
        findViewById<Button>(R.id.btnMissions).setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

        // Recordes vêm do Room, passando por Repository -> ViewModel
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.topScores.collect { tvRecords.text = formatRecords(it) }
            }
        }
    }

    private fun formatRecords(scores: List<ScoreEntity>): String {
        if (scores.isEmpty()) return "Nenhuma partida registrada ainda."
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
        return scores.mapIndexed { index, s ->
            "${index + 1}º  ${s.score} pts  —  ${format.format(Date(s.date))}"
        }.joinToString("\n")
    }
}
