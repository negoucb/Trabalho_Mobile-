package com.example.spacedefender.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.spacedefender.model.GameState
import com.example.spacedefender.model.GameStatus
import com.example.spacedefender.viewmodel.GameViewModel
import kotlin.random.Random

/**
 * Renderiza o jogo com Canvas/Paint em uma thread própria (loop a ~60 FPS).
 * A cada frame chama viewModel.tick(dt) e desenha o último GameState.
 */
class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    private var viewModel: GameViewModel? = null
    private var gameThread: GameThread? = null

    // Fator de escala: o mundo lógico tem largura 1000, a tela tem outra largura em pixels
    private var scale = 1f

    private val density = resources.displayMetrics.density

    private val shipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#4FC3F7") }
    private val flamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FF9800") }
    private val meteorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#8D8D8D") }
    private val craterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#5E5E5E") }
    private val bulletPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FFEB3B") }
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; alpha = 170 }
    private val hudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 20f * density
        isFakeBoldText = true
    }
    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 32f * density
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val shipPath = Path()

    // Estrelas do fundo (x, y, tamanho) em coordenadas do mundo
    private val stars: List<FloatArray> = Random(42).let { rnd ->
        List(70) { floatArrayOf(rnd.nextFloat() * 1000f, rnd.nextFloat() * 3000f, 1f + rnd.nextFloat() * 2.5f) }
    }

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    fun bind(viewModel: GameViewModel) {
        this.viewModel = viewModel
    }

    // ---------- Toque: segurar a tela = atirar ----------

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> viewModel?.setFiring(true)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> viewModel?.setFiring(false)
        }
        return true
    }

    // ---------- Ciclo de vida da Surface ----------

    override fun surfaceCreated(holder: SurfaceHolder) {
        gameThread = GameThread(holder).also { it.start() }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (width == 0) return
        scale = width / WORLD_WIDTH
        viewModel?.setWorldSize(WORLD_WIDTH, height / scale)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        gameThread?.running = false
        try {
            gameThread?.join()
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        gameThread = null
    }

    // ---------- Desenho ----------

    private fun drawGame(canvas: Canvas) {
        val state = viewModel?.state?.value ?: return
        canvas.drawColor(Color.parseColor("#0B0F2A"))

        canvas.save()
        canvas.scale(scale, scale)
        drawStars(canvas, state)
        drawMeteors(canvas, state)
        drawBullets(canvas, state)
        drawShip(canvas, state)
        canvas.restore()

        drawHud(canvas, state)
    }

    private fun drawStars(canvas: Canvas, state: GameState) {
        val offset = (System.nanoTime() / 1_000_000_000f * 60f) % state.worldHeight
        for (s in stars) {
            canvas.drawCircle(s[0], (s[1] + offset) % state.worldHeight, s[2], starPaint)
        }
    }

    private fun drawMeteors(canvas: Canvas, state: GameState) {
        for (m in state.meteors) {
            canvas.drawCircle(m.x, m.y, m.radius, meteorPaint)
            canvas.drawCircle(m.x - m.radius * 0.3f, m.y - m.radius * 0.2f, m.radius * 0.25f, craterPaint)
            canvas.drawCircle(m.x + m.radius * 0.3f, m.y + m.radius * 0.3f, m.radius * 0.18f, craterPaint)
        }
    }

    private fun drawBullets(canvas: Canvas, state: GameState) {
        for (b in state.bullets) {
            canvas.drawRect(b.x - b.radius / 2f, b.y - b.radius * 2f, b.x + b.radius / 2f, b.y + b.radius * 2f, bulletPaint)
        }
    }

    private fun drawShip(canvas: Canvas, state: GameState) {
        // Pisca enquanto está invulnerável depois de levar dano
        if (state.invulnerable && (System.currentTimeMillis() / 100) % 2L == 0L) return

        val s = state.ship
        shipPath.reset()
        shipPath.moveTo(s.x, s.y - s.height / 2f)
        shipPath.lineTo(s.x + s.width / 2f, s.y + s.height / 2f)
        shipPath.lineTo(s.x, s.y + s.height / 3f)
        shipPath.lineTo(s.x - s.width / 2f, s.y + s.height / 2f)
        shipPath.close()
        canvas.drawPath(shipPath, shipPaint)

        // Chama do motor
        shipPath.reset()
        shipPath.moveTo(s.x - s.width / 6f, s.y + s.height / 3f)
        shipPath.lineTo(s.x, s.y + s.height / 3f + s.height / 3f)
        shipPath.lineTo(s.x + s.width / 6f, s.y + s.height / 3f)
        shipPath.close()
        canvas.drawPath(shipPath, flamePaint)
    }

    private fun drawHud(canvas: Canvas, state: GameState) {
        val margin = 16f * density
        canvas.drawText("Pontos: ${state.score}", margin, margin + hudPaint.textSize, hudPaint)

        hudPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Vidas: ${state.lives}", width - margin, margin + hudPaint.textSize, hudPaint)
        hudPaint.textAlign = Paint.Align.LEFT

        if (state.status == GameStatus.PAUSED) {
            canvas.drawText("PAUSADO", width / 2f, height / 2f, centerPaint)
        }
    }

    // ---------- Loop do jogo ----------

    private inner class GameThread(private val surfaceHolder: SurfaceHolder) : Thread("GameThread") {

        @Volatile
        var running = true

        override fun run() {
            var last = System.nanoTime()
            while (running) {
                val frameStart = System.nanoTime()
                val dt = ((frameStart - last) / 1_000_000_000f).coerceAtMost(0.05f)
                last = frameStart

                viewModel?.tick(dt)

                var canvas: Canvas? = null
                try {
                    canvas = surfaceHolder.lockCanvas()
                    if (canvas != null) {
                        synchronized(surfaceHolder) { drawGame(canvas) }
                    }
                } finally {
                    if (canvas != null) surfaceHolder.unlockCanvasAndPost(canvas)
                }

                val frameMs = (System.nanoTime() - frameStart) / 1_000_000L
                if (frameMs < TARGET_FRAME_MS) {
                    try {
                        sleep(TARGET_FRAME_MS - frameMs)
                    } catch (e: InterruptedException) {
                        return
                    }
                }
            }
        }
    }

    companion object {
        private const val WORLD_WIDTH = 1000f
        private const val TARGET_FRAME_MS = 16L
    }
}
