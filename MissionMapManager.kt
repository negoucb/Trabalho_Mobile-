package com.example.spacedefender.map

import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

/** Uma missão exibida no mapa. [difficulty] multiplica a velocidade/frequência dos meteoros. */
data class Mission(
    val id: Int,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val difficulty: Float
)

/**
 * Configura o MapLibre: estilo do mapa, marcadores das missões e seleção por toque.
 * Funciona como módulo independente da lógica da partida.
 */
@Suppress("DEPRECATION")
class MissionMapManager(
    private val mapView: MapView,
    private val onMissionSelected: (Mission) -> Unit
) {

    val missions: List<Mission> = defaultMissions()

    private val markerToMission = mutableMapOf<Long, Mission>()
    private var map: MapLibreMap? = null

    fun setup() {
        mapView.getMapAsync { mapLibreMap ->
            map = mapLibreMap
            mapLibreMap.setStyle(STYLE_URL) {
                mapLibreMap.moveCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(LatLng(-14.2, -51.9)) // centro aproximado do Brasil
                            .zoom(3.0)
                            .build()
                    )
                )
                addMarkers(mapLibreMap)
                mapLibreMap.setOnMarkerClickListener { marker ->
                    markerToMission[marker.id]?.let { selectMission(it) }
                    true
                }
            }
        }
    }

    private fun addMarkers(mapLibreMap: MapLibreMap) {
        for (mission in missions) {
            val marker = mapLibreMap.addMarker(
                MarkerOptions()
                    .position(LatLng(mission.latitude, mission.longitude))
                    .title(mission.name)
                    .snippet(mission.description)
            )
            markerToMission[marker.id] = mission
        }
    }

    private fun selectMission(mission: Mission) {
        map?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(mission.latitude, mission.longitude), 5.0)
        )
        onMissionSelected(mission)
    }

    companion object {
        const val STYLE_URL = "https://demotiles.maplibre.org/style.json"

        /** Locais aproximados ligados ao tema espacial no Brasil. */
        fun defaultMissions(): List<Mission> = listOf(
            Mission(1, "Base Brasília", "Missão de treinamento: poucos meteoros.", -15.7939, -47.8828, 0.8f),
            Mission(2, "Observatório Nacional (RJ)", "Meteoros em ritmo moderado.", -22.8956, -43.2238, 1.0f),
            Mission(3, "INPE — São José dos Campos", "Mais meteoros e mais rápidos.", -23.2076, -45.8606, 1.2f),
            Mission(4, "Barreira do Inferno (RN)", "Chuva de meteoros intensa.", -5.9230, -35.1608, 1.4f),
            Mission(5, "Centro de Lançamento de Alcântara (MA)", "Missão final: dificuldade máxima.", -2.3733, -44.3967, 1.6f)
        )
    }
}
