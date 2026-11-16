package com.example.spacedefender.model

/** Meteoro que cai do topo da tela. */
data class Meteor(
    var x: Float,
    var y: Float,
    val radius: Float,
    val speed: Float,
    var alive: Boolean = true
)
