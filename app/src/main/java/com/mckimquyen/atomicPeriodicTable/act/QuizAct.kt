package com.mckimquyen.atomicPeriodicTable.act

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.databinding.AQuizBinding
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.model.ElementModel
import com.mckimquyen.atomicPeriodicTable.util.CategoryTranslator
import com.mckimquyen.atomicPeriodicTable.util.ElementWeightCache
import com.mckimquyen.atomicPeriodicTable.util.Utils
import java.util.Random
import android.animation.ValueAnimator
import android.animation.ArgbEvaluator

class QuizAct : BaseAct() {

    private lateinit var binding: AQuizBinding
    private val elementsList = ArrayList<Element>()
    private val random = Random()

    private var currentQuestionIndex = 0
    private val totalQuestions = 10
    private var score = 0

    private lateinit var optionCards: List<CardView>
    private lateinit var optionTexts: List<TextView>

    private var defaultCardTint: ColorStateList? = null
    private var defaultTextColor: ColorStateList? = null

    private var currentCorrectAnswer = ""
    private val currentChoices = ArrayList<String>()
    private var isAnswered = false
    private var countDownTimer: android.os.CountDownTimer? = null
    private val maxTimeSeconds = 15

    private var gradientAnimator: ValueAnimator? = null
    private var restartPulseAnimator: ValueAnimator? = null
    private var currentGradientIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize elements
        ElementModel.getList(elementsList)

        setupViews()
        startBackgroundAnimation()
        
        // Smooth entrance slide-up and fade-in for content container
        binding.quizScrollView.alpha = 0f
        binding.quizScrollView.translationY = 50f
        binding.quizScrollView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        startQuiz()
    }

    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        val params = binding.commonTitleBack.layoutParams
        params.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.commonTitleBack.layoutParams = params

        val topPadding = resources.getDimensionPixelSize(R.dimen.margin)
        val bottomPadding = bottom + resources.getDimensionPixelSize(R.dimen.margin)
        binding.quizScrollView.setPadding(0, topPadding, 0, bottomPadding)
    }

    private fun setupViews() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        optionCards = listOf(
            binding.option1Card,
            binding.option2Card,
            binding.option3Card,
            binding.option4Card
        )

        optionTexts = listOf(
            binding.option1Text,
            binding.option2Text,
            binding.option3Text,
            binding.option4Text
        )

        defaultCardTint = binding.option1Card.backgroundTintList
        defaultTextColor = binding.option1Text.textColors

        for (i in optionCards.indices) {
            val card = optionCards[i]
            card.setOnClickListener {
                if (!isAnswered) {
                    checkAnswer(i)
                }
            }

            // Snappy press touch-scaling feedback for options
            card.setOnTouchListener { v, event ->
                if (!isAnswered) {
                    when (event.action) {
                        android.view.MotionEvent.ACTION_DOWN -> {
                            v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(100).start()
                        }
                        android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                            v.animate().scaleX(1f).scaleY(1f).setDuration(150)
                                .setInterpolator(android.view.animation.OvershootInterpolator()).start()
                        }
                    }
                }
                false
            }
        }

        binding.quizRestartBtn.setOnClickListener {
            startQuiz()
        }
    }

    private fun startQuiz() {
        currentQuestionIndex = 0
        score = 0
        binding.quizProgressBar.progress = 0
        binding.confettiView.stopConfetti()
        
        restartPulseAnimator?.cancel()
        binding.quizRestartBtn.scaleX = 1f
        binding.quizRestartBtn.scaleY = 1f
        
        if (binding.quizResultCard.visibility == View.VISIBLE) {
            binding.quizResultCard.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(250)
                .withEndAction {
                    binding.quizResultCard.visibility = View.GONE
                    binding.questionCard.visibility = View.VISIBLE
                    binding.optionsContainer.visibility = View.VISIBLE
                    binding.quizProgress.visibility = View.VISIBLE
                    binding.quizScore.visibility = View.VISIBLE
                    
                    binding.questionCard.alpha = 1f
                    binding.questionCard.scaleX = 1f
                    binding.questionCard.scaleY = 1f
                    binding.optionsContainer.alpha = 1f
                    binding.optionsContainer.scaleX = 1f
                    binding.optionsContainer.scaleY = 1f
                    
                    generateQuestion()
                }
                .start()
        } else {
            binding.questionCard.visibility = View.VISIBLE
            binding.optionsContainer.visibility = View.VISIBLE
            binding.quizResultCard.visibility = View.GONE
            binding.quizProgress.visibility = View.VISIBLE
            binding.quizScore.visibility = View.VISIBLE
            
            generateQuestion()
        }
    }

    private fun setupQuestionData() {
        // Pick target element
        val targetElement = elementsList[random.nextInt(elementsList.size)]
        val questionType = random.nextInt(6) // 0: Atomic Number, 1: Symbol, 2: Category, 3: Name from Symbol, 4: Isotopes, 5: Electronegativity

        // Generate choices
        currentChoices.clear()
        val formattedElementName = com.mckimquyen.atomicPeriodicTable.util.ElementTranslator.getLocalizedName(this, targetElement.element)

        when (questionType) {
            0 -> {
                // Atomic number
                binding.questionText.text = getString(R.string.quiz_question_atomic).format(formattedElementName)
                currentCorrectAnswer = targetElement.number.toString()
                currentChoices.add(currentCorrectAnswer)

                while (currentChoices.size < 4) {
                    val wrongNum = random.nextInt(118) + 1
                    val wrongStr = wrongNum.toString()
                    if (!currentChoices.contains(wrongStr)) {
                        currentChoices.add(wrongStr)
                    }
                }
            }
            1 -> {
                // Symbol
                binding.questionText.text = getString(R.string.quiz_question_symbol).format(formattedElementName)
                currentCorrectAnswer = targetElement.short
                currentChoices.add(currentCorrectAnswer)

                while (currentChoices.size < 4) {
                    val wrongEl = elementsList[random.nextInt(elementsList.size)]
                    if (wrongEl.short != currentCorrectAnswer && !currentChoices.contains(wrongEl.short)) {
                        currentChoices.add(wrongEl.short)
                    }
                }
            }
            2 -> {
                // Category
                binding.questionText.text = getString(R.string.quiz_question_category).format(formattedElementName)
                val rawCategory = ElementWeightCache.getCategory(targetElement.short) ?: "Other Nonmetals"
                currentCorrectAnswer = CategoryTranslator.translate(this, rawCategory)
                currentChoices.add(currentCorrectAnswer)

                val categories = listOf(
                    "Other Nonmetals", "Noble Gases", "Alkali Metals", "Alkaline Earth Metals",
                    "Transition Metals", "Lanthanides", "Actinides", "Post-transition Metals", "Metalloids", "Halogens"
                ).map { CategoryTranslator.translate(this, it) }.toMutableSet()

                categories.remove(currentCorrectAnswer)

                val wrongList = categories.toList().shuffled()
                for (i in 0 until minOf(3, wrongList.size)) {
                    currentChoices.add(wrongList[i])
                }
                while (currentChoices.size < 4) {
                    currentChoices.add("---")
                }
            }
            3 -> {
                // Name from Symbol
                binding.questionText.text = getString(R.string.quiz_question_name_from_symbol).format(targetElement.short)
                currentCorrectAnswer = formattedElementName
                currentChoices.add(currentCorrectAnswer)

                while (currentChoices.size < 4) {
                    val wrongEl = elementsList[random.nextInt(elementsList.size)]
                    val wrongName = com.mckimquyen.atomicPeriodicTable.util.ElementTranslator.getLocalizedName(this, wrongEl.element)
                    if (wrongName != currentCorrectAnswer && !currentChoices.contains(wrongName)) {
                        currentChoices.add(wrongName)
                    }
                }
            }
            4 -> {
                // Isotopes count
                binding.questionText.text = getString(R.string.quiz_question_isotopes).format(formattedElementName)
                currentCorrectAnswer = targetElement.isotopes.toString()
                currentChoices.add(currentCorrectAnswer)

                while (currentChoices.size < 4) {
                    val wrongNum = maxOf(0, targetElement.isotopes + random.nextInt(15) - 7)
                    val wrongStr = wrongNum.toString()
                    if (!currentChoices.contains(wrongStr)) {
                        currentChoices.add(wrongStr)
                    }
                }
            }
            else -> {
                // Electronegativity (fallback if 0.0)
                if (targetElement.electro == 0.0) {
                    binding.questionText.text = getString(R.string.quiz_question_symbol).format(formattedElementName)
                    currentCorrectAnswer = targetElement.short
                    currentChoices.add(currentCorrectAnswer)

                    while (currentChoices.size < 4) {
                        val wrongEl = elementsList[random.nextInt(elementsList.size)]
                        if (wrongEl.short != currentCorrectAnswer && !currentChoices.contains(wrongEl.short)) {
                            currentChoices.add(wrongEl.short)
                        }
                    }
                } else {
                    binding.questionText.text = getString(R.string.quiz_question_electronegativity).format(formattedElementName)
                    currentCorrectAnswer = String.format(java.util.Locale.US, "%.2f", targetElement.electro)
                    currentChoices.add(currentCorrectAnswer)

                    while (currentChoices.size < 4) {
                        val wrongEl = elementsList[random.nextInt(elementsList.size)]
                        if (wrongEl.electro > 0.0) {
                            val wrongStr = String.format(java.util.Locale.US, "%.2f", wrongEl.electro)
                            if (!currentChoices.contains(wrongStr)) {
                                currentChoices.add(wrongStr)
                            }
                        }
                    }
                }
            }
        }

        currentChoices.shuffle()
    }

    private fun generateQuestion() {
        if (currentQuestionIndex >= totalQuestions) {
            showResults()
            return
        }

        isAnswered = false
        countDownTimer?.cancel()

        // Update headers
        binding.quizProgress.text = getString(R.string.quiz_progress).format(currentQuestionIndex + 1, totalQuestions)
        binding.quizScore.text = getString(R.string.quiz_score).format(score, totalQuestions)

        // Animate progress bar width
        val progressPercent = (currentQuestionIndex * 100) / totalQuestions
        android.animation.ObjectAnimator.ofInt(binding.quizProgressBar, "progress", progressPercent)
            .setDuration(400)
            .apply {
                interpolator = android.view.animation.DecelerateInterpolator()
                start()
            }

        // Staggered slide out/in carousel transition between questions
        if (currentQuestionIndex > 0) {
            val duration = 250L
            binding.questionCard.animate()
                .alpha(0f)
                .translationX(-300f)
                .setDuration(duration)
                .setInterpolator(android.view.animation.AccelerateInterpolator())
                .start()

            binding.optionsContainer.animate()
                .alpha(0f)
                .translationX(-300f)
                .setDuration(duration)
                .setInterpolator(android.view.animation.AccelerateInterpolator())
                .withEndAction {
                    setupQuestionData()
                    
                    binding.questionCard.translationX = 300f
                    binding.optionsContainer.translationX = 300f
                    
                    binding.questionCard.animate()
                        .alpha(1f)
                        .translationX(0f)
                        .setDuration(300)
                        .setInterpolator(android.view.animation.DecelerateInterpolator())
                        .start()

                    for (i in optionCards.indices) {
                        optionCards[i].backgroundTintList = defaultCardTint
                        defaultTextColor?.let { optionTexts[i].setTextColor(it) }
                        optionTexts[i].text = currentChoices[i]
                        
                        optionCards[i].alpha = 0f
                        optionCards[i].translationX = 150f
                        optionCards[i].translationY = 0f
                        optionCards[i].scaleX = 0.9f
                        optionCards[i].scaleY = 0.9f

                        optionCards[i].animate()
                            .alpha(1f)
                            .translationX(0f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setStartDelay(i * 50L)
                            .setDuration(350)
                            .setInterpolator(android.view.animation.OvershootInterpolator(1.1f))
                            .start()
                    }
                    binding.optionsContainer.translationX = 0f
                    binding.optionsContainer.alpha = 1f
                    
                    startQuestionTimer()
                }
                .start()
        } else {
            setupQuestionData()
            
            binding.questionCard.alpha = 0f
            binding.questionCard.translationY = -30f
            binding.questionCard.translationX = 0f
            binding.questionCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()

            binding.optionsContainer.translationX = 0f
            binding.optionsContainer.alpha = 1f

            for (i in optionCards.indices) {
                optionCards[i].backgroundTintList = defaultCardTint
                defaultTextColor?.let { optionTexts[i].setTextColor(it) }
                optionTexts[i].text = currentChoices[i]
                
                optionCards[i].alpha = 0f
                optionCards[i].translationY = 50f
                optionCards[i].translationX = 0f
                optionCards[i].scaleX = 0.85f
                optionCards[i].scaleY = 0.85f

                optionCards[i].animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(i * 80L)
                    .setDuration(350)
                    .setInterpolator(android.view.animation.OvershootInterpolator(1.2f))
                    .start()
            }
            
            startQuestionTimer()
        }
    }

    private fun checkAnswer(selectedIndex: Int) {
        isAnswered = true
        countDownTimer?.cancel()
        val selectedAnswer = currentChoices[selectedIndex]
        val correctIndex = currentChoices.indexOf(currentCorrectAnswer)
        val clickedCard = optionCards[selectedIndex]

        // Resolve theme colors dynamically for light/dark mode
        val isDark = when (com.mckimquyen.atomicPeriodicTable.pref.ThemePref(this).getValue()) {
            100 -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            1 -> true
            else -> false
        }

        val correctBg = if (isDark) Color.parseColor("#1B5E20") else Color.parseColor("#C8E6C9")
        val correctText = if (isDark) Color.parseColor("#E8F5E9") else Color.parseColor("#1B5E20")

        val incorrectBg = if (isDark) Color.parseColor("#B71C1C") else Color.parseColor("#FFCDD2")
        val incorrectText = if (isDark) Color.parseColor("#FFEBEE") else Color.parseColor("#B71C1C")

        if (selectedAnswer == currentCorrectAnswer) {
            score++
            optionCards[selectedIndex].backgroundTintList = ColorStateList.valueOf(correctBg)
            optionTexts[selectedIndex].setTextColor(correctText)
            
            // Trigger 40-particle correct celebration burst from center of button
            val rect = android.graphics.Rect()
            clickedCard.getGlobalVisibleRect(rect)
            val confettiLocation = IntArray(2)
            binding.confettiView.getLocationInWindow(confettiLocation)
            val clickX = rect.centerX().toFloat() - confettiLocation[0]
            val clickY = rect.centerY().toFloat() - confettiLocation[1]
            binding.confettiView.startBurst(clickX, clickY)

            // Dynamic scale bounce on score text update
            binding.quizScore.animate()
                .scaleX(1.3f)
                .scaleY(1.3f)
                .setDuration(150)
                .withEndAction {
                    binding.quizScore.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start()
                }
                .start()
        } else {
            optionCards[selectedIndex].backgroundTintList = ColorStateList.valueOf(incorrectBg)
            optionTexts[selectedIndex].setTextColor(incorrectText)
            if (correctIndex != -1) {
                optionCards[correctIndex].backgroundTintList = ColorStateList.valueOf(correctBg)
                optionTexts[correctIndex].setTextColor(correctText)
            }
        }

        // Spring scale up and rotation for the correct answer
        val correctCard = if (correctIndex != -1) optionCards[correctIndex] else null
        correctCard?.let { card ->
            card.cardElevation = 10f * resources.displayMetrics.density
            card.animate()
                .scaleX(1.08f)
                .scaleY(1.08f)
                .rotation((random.nextFloat() * 4f) - 2f)
                .setDuration(220)
                .setInterpolator(android.view.animation.OvershootInterpolator(3.0f))
                .withEndAction {
                    card.animate()
                        .scaleX(1.04f)
                        .scaleY(1.04f)
                        .rotation(0f)
                        .setDuration(120)
                        .withEndAction {
                            card.cardElevation = 0f
                        }
                        .start()
                }
                .start()
        }

        // Physical spring shake for the incorrect selected choice
        if (selectedAnswer != currentCorrectAnswer) {
            val wrongCard = optionCards[selectedIndex]
            wrongCard.cardElevation = 6f * resources.displayMetrics.density
            val shakeAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 500
                addUpdateListener { animator ->
                    val fraction = animator.animatedValue as Float
                    val translation = 24f * kotlin.math.sin(fraction * 4 * Math.PI.toFloat()) * (1f - fraction)
                    wrongCard.translationX = translation
                }
                addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        wrongCard.translationX = 0f
                        wrongCard.cardElevation = 0f
                    }
                })
            }
            shakeAnimator.start()
        }

        // Dim and shrink other unselected incorrect option cards
        for (i in optionCards.indices) {
            if (i != correctIndex && (i != selectedIndex || selectedAnswer == currentCorrectAnswer)) {
                optionCards[i].animate()
                    .alpha(0.5f)
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(250)
                    .start()
            }
        }

        // Delay proceed
        binding.root.postDelayed({
            currentQuestionIndex++
            generateQuestion()
        }, 1500)
    }

    private fun animateBounce(view: View) {
        view.animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(150)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }

    private fun showResults() {
        // Fade out quiz components smoothly
        binding.questionCard.animate().alpha(0f).scaleX(0.8f).scaleY(0.8f).setDuration(300).start()
        binding.optionsContainer.animate().alpha(0f).scaleX(0.8f).scaleY(0.8f).setDuration(300).withEndAction {
            binding.questionCard.visibility = View.GONE
            binding.optionsContainer.visibility = View.GONE
            binding.quizProgress.visibility = View.GONE
            binding.quizScore.visibility = View.GONE

            binding.quizResultCard.alpha = 0f
            binding.quizResultCard.scaleX = 0.6f
            binding.quizResultCard.scaleY = 0.6f
            binding.quizResultCard.visibility = View.VISIBLE

            binding.quizResultCard.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setInterpolator(android.view.animation.OvershootInterpolator(1.4f))
                .withEndAction {
                    binding.confettiView.startConfetti()
                    
                    // Score count-up ticker
                    val scoreAnimator = ValueAnimator.ofInt(0, score).apply {
                        duration = 1200
                        setInterpolator(android.view.animation.DecelerateInterpolator())
                        addUpdateListener { animator ->
                            val currentVal = animator.animatedValue as Int
                            binding.quizResultText.text = getString(R.string.quiz_finished).format(currentVal, totalQuestions)
                        }
                    }
                    scoreAnimator.start()

                    // Restart button pulse animation
                    restartPulseAnimator?.cancel()
                    restartPulseAnimator = ValueAnimator.ofFloat(1f, 1.08f).apply {
                        duration = 1000
                        repeatMode = ValueAnimator.REVERSE
                        repeatCount = ValueAnimator.INFINITE
                        addUpdateListener { animator ->
                            val scale = animator.animatedValue as Float
                            binding.quizRestartBtn.scaleX = scale
                            binding.quizRestartBtn.scaleY = scale
                        }
                    }
                    restartPulseAnimator?.start()
                }
                .start()
        }.start()
    }

    private fun startBackgroundAnimation() {
        val isDark = when (com.mckimquyen.atomicPeriodicTable.pref.ThemePref(this).getValue()) {
            1 -> true
            0 -> false
            else -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        }
        val startColor = if (isDark) Color.parseColor("#120C1F") else Color.parseColor("#F3F8FC")
        val endColor = if (isDark) Color.parseColor("#09101C") else Color.parseColor("#E0F2FE")

        val gd = android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
            intArrayOf(startColor, endColor)
        )
        binding.root.background = gd
    }

    private fun startQuestionTimer() {
        countDownTimer?.cancel()
        
        binding.timerProgress.max = maxTimeSeconds * 1000
        binding.timerProgress.progress = maxTimeSeconds * 1000
        binding.timerText.text = maxTimeSeconds.toString()
        
        binding.timerText.scaleX = 1.0f
        binding.timerText.scaleY = 1.0f
        binding.timerText.setTextColor(getColorFromAttr(R.attr.colorOnSurface))
        binding.timerProgress.setIndicatorColor(getColorFromAttr(R.attr.colorPrimary))

        countDownTimer = object : android.os.CountDownTimer((maxTimeSeconds * 1000).toLong(), 50) {
            override fun onTick(millisUntilFinished: Long) {
                val progress = millisUntilFinished.toInt()
                binding.timerProgress.progress = progress
                
                val secondsRemaining = (millisUntilFinished / 1000).toInt() + 1
                binding.timerText.text = secondsRemaining.toString()

                // Smooth color interpolation of the timer indicator from green -> orange -> red
                val fraction = progress.toFloat() / (maxTimeSeconds * 1000f) // 1.0 down to 0.0
                val color = if (fraction > 0.5f) {
                    val localFraction = (fraction - 0.5f) * 2f // 1.0 down to 0.0
                    ArgbEvaluator().evaluate(1f - localFraction, Color.parseColor("#4CAF50"), Color.parseColor("#FF9800")) as Int
                } else {
                    val localFraction = fraction * 2f // 1.0 down to 0.0
                    ArgbEvaluator().evaluate(1f - localFraction, Color.parseColor("#FF9800"), Color.parseColor("#F44336")) as Int
                }
                binding.timerProgress.setIndicatorColor(color)
                binding.timerText.setTextColor(color)

                if (secondsRemaining <= 5) {
                    val pulse = 1.0f + 0.12f * kotlin.math.sin((millisUntilFinished.toDouble() / 120.0)).toFloat()
                    binding.timerText.scaleX = pulse
                    binding.timerText.scaleY = pulse
                }
            }

            override fun onFinish() {
                binding.timerProgress.progress = 0
                binding.timerText.text = "0"
                binding.timerText.scaleX = 1.0f
                binding.timerText.scaleY = 1.0f
                handleTimesUp()
            }
        }.start()
    }

    private fun handleTimesUp() {
        isAnswered = true
        val correctIndex = currentChoices.indexOf(currentCorrectAnswer)

        val isDark = when (com.mckimquyen.atomicPeriodicTable.pref.ThemePref(this).getValue()) {
            100 -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            1 -> true
            else -> false
        }
        val correctBg = if (isDark) Color.parseColor("#1B5E20") else Color.parseColor("#C8E6C9")
        val correctText = if (isDark) Color.parseColor("#E8F5E9") else Color.parseColor("#1B5E20")

        if (correctIndex != -1) {
            optionCards[correctIndex].backgroundTintList = ColorStateList.valueOf(correctBg)
            optionTexts[correctIndex].setTextColor(correctText)
            
            optionCards[correctIndex].let { card ->
                card.cardElevation = 10f * resources.displayMetrics.density
                card.animate()
                    .scaleX(1.08f)
                    .scaleY(1.08f)
                    .setDuration(220)
                    .setInterpolator(android.view.animation.OvershootInterpolator(3.0f))
                    .withEndAction {
                        card.animate()
                            .scaleX(1.04f)
                            .scaleY(1.04f)
                            .setDuration(120)
                            .withEndAction {
                                card.cardElevation = 0f
                            }
                            .start()
                    }
                    .start()
            }
        }

        for (i in optionCards.indices) {
            if (i != correctIndex) {
                optionCards[i].animate()
                    .alpha(0.5f)
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(250)
                    .start()
            }
        }

        binding.root.postDelayed({
            currentQuestionIndex++
            generateQuestion()
        }, 1500)
    }

    private fun getColorFromAttr(attr: Int): Int {
        val typedValue = android.util.TypedValue()
        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        gradientAnimator?.cancel()
        restartPulseAnimator?.cancel()
        super.onDestroy()
    }
}
