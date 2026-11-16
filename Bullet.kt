package com.example.spacedefender.model

/** Projétil disparado pela nave (sobe na tela). */
data class Bullet(
    var x: Float,
    var y: Float,
    val speed: Float = 1100f,
    val radius: Float = 8f,
    var alive: Boolean = true
)
