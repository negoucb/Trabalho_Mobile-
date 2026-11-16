package com.example.spacedefender.game

import com.example.spacedefender.model.Bullet
import com.example.spacedefender.model.Meteor
import com.example.spacedefender.model.Spaceship

/**
 * Detecção de colisões usando círculos (distância entre centros <= soma dos raios).
 */
class CollisionManager {

    fun circlesOverlap(
        x1: Float, y1: Float, r1: Float,
        x2: Float, y2: Float, r2: Float
    ): Boolean {
        val dx = x1 - x2
        val dy = y1 - y2
        val sum = r1 + r2
        return dx * dx + dy * dy <= sum * sum
    }

    fun bulletHitsMeteor(bullet: Bullet, meteor: Meteor): Boolean =
        circlesOverlap(bullet.x, bullet.y, bullet.radius, meteor.x, meteor.y, meteor.radius)

    fun shipHitsMeteor(ship: Spaceship, meteor: Meteor): Boolean =
        circlesOverlap(ship.x, ship.y, ship.radius, meteor.x, meteor.y, meteor.radius)
}
