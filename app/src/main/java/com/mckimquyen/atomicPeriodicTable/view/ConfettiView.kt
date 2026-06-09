package com.mckimquyen.atomicPeriodicTable.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import java.util.Random

class ConfettiView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val random = Random()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val particles = ArrayList<Particle>()
    private var animator: ValueAnimator? = null
    
    private val colors = intArrayOf(
        Color.parseColor("#FF1744"), // Red
        Color.parseColor("#D500F9"), // Purple
        Color.parseColor("#2979FF"), // Blue
        Color.parseColor("#00E676"), // Green
        Color.parseColor("#FFEA00"), // Yellow
        Color.parseColor("#FF9100")  // Orange
    )

    private class Particle(
        var x: Float,
        var y: Float,
        var size: Float,
        var color: Int,
        var speedX: Float,
        var speedY: Float,
        var rotation: Float,
        var rotationSpeed: Float
    )

    fun startConfetti() {
        animator?.cancel()
        particles.clear()
        
        val width = width.toFloat()
        if (width <= 0) {
            post { startConfetti() }
            return
        }

        // Spawn 80 particles
        for (i in 0 until 80) {
            val size = (12..28).random().toFloat()
            particles.add(
                Particle(
                    x = random.nextFloat() * width,
                    y = -50f - (random.nextFloat() * 200f),
                    size = size,
                    color = colors[random.nextInt(colors.size)],
                    speedX = (random.nextFloat() * 4f) - 2f,
                    speedY = (6f + random.nextFloat() * 8f),
                    rotation = random.nextFloat() * 360f,
                    rotationSpeed = (random.nextFloat() * 10f) - 5f
                )
            )
        }

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 4000
            interpolator = LinearInterpolator()
            addUpdateListener {
                updateParticles()
                invalidate()
            }
            start()
        }
    }

    private fun updateParticles() {
        val height = height.toFloat()
        val width = width.toFloat()
        for (p in particles) {
            p.y += p.speedY
            p.x += p.speedX
            p.rotation += p.rotationSpeed
            if (p.y > height) {
                p.y = -50f
                p.x = random.nextFloat() * width
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (p in particles) {
            paint.color = p.color
            canvas.save()
            canvas.translate(p.x, p.y)
            canvas.rotate(p.rotation)
            canvas.drawRect(-p.size / 2, -p.size, p.size / 2, p.size, paint)
            canvas.restore()
        }
    }

    private fun ClosedRange<Int>.random() = random.nextInt(endInclusive - start) + start
}
