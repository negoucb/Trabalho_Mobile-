package com.example.spacedefender.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.spacedefender.R
import com.example.spacedefender.map.Mission
import com.example.spacedefender.map.MissionMapManager
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapView

/** Tela de missões: mapa com marcadores; ao escolher uma, inicia a partida com a dificuldade dela. */
class MapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var missionManager: MissionMapManager
    private var selectedMission: Mission? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this) // precisa ser chamado antes de inflar o MapView
        setContentView(R.layout.activity_map)

        val tvName = findViewById<TextView>(R.id.tvMissionName)
        val tvDesc = findViewById<TextView>(R.id.tvMissionDesc)
        val btnStart = findViewById<Button>(R.id.btnStartMission)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        missionManager = MissionMapManager(mapView) { mission ->
            selectedMission = mission
            tvName.text = mission.name
            tvDesc.text = "${mission.description}\nDificuldade: x${mission.difficulty}"
            btnStart.isEnabled = true
        }
        missionManager.setup()

        btnStart.setOnClickListener {
            val mission = selectedMission ?: return@setOnClickListener
            startActivity(
                Intent(this, GameActivity::class.java)
                    .putExtra(GameActivity.EXTRA_DIFFICULTY, mission.difficulty)
            )
        }
    }

    // O MapView precisa receber todos os eventos do ciclo de vida
    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { mapView.onPause(); super.onPause() }
    override fun onStop() { mapView.onStop(); super.onStop() }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
    override fun onDestroy() { mapView.onDestroy(); super.onDestroy() }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
