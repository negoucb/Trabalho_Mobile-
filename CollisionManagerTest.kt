package com.example.spacedefender

import com.example.spacedefender.game.CollisionManager
import com.example.spacedefender.model.Bullet
import com.example.spacedefender.model.Meteor
import com.example.spacedefender.model.Spaceship
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CollisionManagerTest {

    private val collisions = CollisionManager()

    @Test
    fun circulosSobrepostos_colidem() {
        assertTrue(collisions.circlesOverlap(0f, 0f, 10f, 5f, 0f, 10f))
    }

    @Test
    fun circulosDistantes_naoColidem() {
        assertFalse(collisions.circlesOverlap(0f, 0f, 10f, 100f, 100f, 10f))
    }

    @Test
    fun circulosSeTocandoNaBorda_colidem() {
        assertTrue(collisions.circlesOverlap(0f, 0f, 10f, 20f, 0f, 10f))
    }

    @Test
    fun disparoAcertaMeteoro() {
        val meteor = Meteor(x = 500f, y = 300f, radius = 40f, speed = 0f)
        val bullet = Bullet(x = 510f, y = 310f)
        assertTrue(collisions.bulletHitsMeteor(bullet, meteor))
    }

    @Test
    fun disparoLongeDoMeteoro_naoAcerta() {
        val meteor = Meteor(x = 500f, y = 300f, radius = 40f, speed = 0f)
        val bullet = Bullet(x = 100f, y = 900f)
        assertFalse(collisions.bulletHitsMeteor(bullet, meteor))
    }

    @Test
    fun meteoroAtingeNave() {
        val ship = Spaceship(x = 500f, y = 1500f)
        val meteor = Meteor(x = 500f, y = 1490f, radius = 40f, speed = 0f)
        assertTrue(collisions.shipHitsMeteor(ship, meteor))
    }

    @Test
    fun meteoroLongeDaNave_naoAtinge() {
        val ship = Spaceship(x = 500f, y = 1500f)
        val meteor = Meteor(x = 100f, y = 100f, radius = 40f, speed = 0f)
        assertFalse(collisions.shipHitsMeteor(ship, meteor))
    }
}
