package com.example.spacedefender.model

/**
 * Nave do jogador. Coordenadas no "mundo lógico" do jogo (largura fixa de 1000 unidades).
 * (x, y) é o centro da nave.
 */
data class Spaceship(
    var x: Float,
    var y: Float,
    val width: Float = 90f,
    val height: Float = 110f
) {
    /** Raio usado na detecção de colisão (um pouco menor que a nave para ser mais justo). */
    val radius: Float get() = width * 0.4f
}
