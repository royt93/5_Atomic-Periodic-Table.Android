package com.mckimquyen.atomicPeriodicTable.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
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
    private var isBurst = false
    
    private val colors = intArrayOf(
        Color.parseColor("#FF1744"), // Vibrant Red
        Color.parseColor("#D500F9"), // Vibrant Purple
        Color.parseColor("#2979FF"), // Vibrant Blue
        Color.parseColor("#00E676"), // Vibrant Green
        Color.parseColor("#FFEA00"), // Vibrant Yellow
        Color.parseColor("#FF9100"), // Vibrant Orange
        Color.parseColor("#00E5FF"), // Neon Cyan
        Color.parseColor("#FF4081")  // Neon Pink
    )

    private class Particle(
        var x: Float,
        var y: Float,
        var size: Float,
        var color: Int,
        var speedX: Float,
        var speedY: Float,
        var rotation: Float,
        var rotationSpeed: Float,
        var shapeType: Int, // 0: Rectangle, 1: Circle, 2: Triangle
        var alpha: Int = 255,
        val swaySpeed: Float = 0.05f + Random().nextFloat() * 0.1f,
        val swayOffset: Float = Random().nextFloat() * 100f
    )

    fun startConfetti() {
        isBurst = false
        animator?.cancel()
        particles.clear()
        
        val width = width.toFloat()
        if (width <= 0) {
            post { startConfetti() }
            return
        }

        // Spawn 100 particles for a richer, more celebratory falling confetti rain
        for (i in 0 until 100) {
            val size = (12..28).random().toFloat()
            particles.add(
                Particle(
                    x = random.nextFloat() * width,
                    y = -50f - (random.nextFloat() * 300f),
                    size = size,
                    color = colors[random.nextInt(colors.size)],
                    speedX = (random.nextFloat() * 3f) - 1.5f,
                    speedY = (4f + random.nextFloat() * 6f),
                    rotation = random.nextFloat() * 360f,
                    rotationSpeed = (random.nextFloat() * 8f) - 4f,
                    shapeType = random.nextInt(3)
                )
            )
        }

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 5000
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animator ->
                val fraction = animator.animatedValue as Float
                if (isBurst) {
                    updateParticlesGravity(fraction)
                } else {
                    updateParticles()
                }
                invalidate()
            }
            start()
        }
    }

    fun startBurst(x: Float, y: Float) {
        isBurst = true
        animator?.cancel()
        particles.clear()
        
        // Spawn 40 particles for a dense, satisfying explosion
        for (i in 0 until 40) {
            val size = (10..24).random().toFloat()
            val angle = random.nextFloat() * 2 * Math.PI
            val speed = 5f + random.nextFloat() * 10f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    size = size,
                    color = colors[random.nextInt(colors.size)],
                    speedX = (speed * kotlin.math.cos(angle)).toFloat(),
                    speedY = (speed * kotlin.math.sin(angle)).toFloat() - 5f, // strong initial upward burst
                    rotation = random.nextFloat() * 360f,
                    rotationSpeed = (random.nextFloat() * 16f) - 8f,
                    shapeType = random.nextInt(3)
                )
            )
        }

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1400
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                val fraction = animator.animatedValue as Float
                updateParticlesGravity(fraction)
                invalidate()
            }
            start()
        }
    }

    fun stopConfetti() {
        animator?.cancel()
        particles.clear()
        invalidate()
    }

    private fun updateParticles() {
        val height = height.toFloat()
        val width = width.toFloat()
        for (p in particles) {
            p.y += p.speedY
            // Flutter side to side organically using a sine wave based on current y coordinate
            val sway = kotlin.math.sin(p.y * p.swaySpeed + p.swayOffset) * 1.8f
            p.x += p.speedX + sway
            p.rotation += p.rotationSpeed
            
            // Loop around if confetti hits bottom
            if (p.y > height) {
                p.y = -50f
                p.x = random.nextFloat() * width
            }
        }
    }

    private fun updateParticlesGravity(fraction: Float) {
        for (p in particles) {
            p.speedY += 0.3f // gravity acceleration
            p.speedX *= 0.97f // air resistance
            p.y += p.speedY
            p.x += p.speedX
            p.rotation += p.rotationSpeed
            
            // Smoothly fade out particles during the second half of the burst
            p.alpha = if (fraction < 0.4f) {
                255
            } else {
                val fadeFraction = (fraction - 0.4f) / 0.6f
                ((1f - fadeFraction) * 255).toInt().coerceIn(0, 255)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (p in particles) {
            paint.color = p.color
            paint.alpha = p.alpha
            canvas.save()
            canvas.translate(p.x, p.y)
            canvas.rotate(p.rotation)
            
            when (p.shapeType) {
                0 -> { // Rectangle
                    canvas.drawRect(-p.size / 2, -p.size, p.size / 2, p.size, paint)
                }
                1 -> { // Circle
                    canvas.drawCircle(0f, 0f, p.size / 2, paint)
                }
                2 -> { // Triangle
                    val path = Path().apply {
                        moveTo(0f, -p.size)
                        lineTo(-p.size / 2, p.size / 2)
                        lineTo(p.size / 2, p.size / 2)
                        close()
                    }
                    canvas.drawPath(path, paint)
                }
            }
            canvas.restore()
        }
    }
}

