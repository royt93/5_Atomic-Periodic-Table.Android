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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize elements
        ElementModel.getList(elementsList)

        setupViews()
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
            optionCards[i].setOnClickListener {
                if (!isAnswered) {
                    checkAnswer(i)
                }
            }
        }

        binding.quizRestartBtn.setOnClickListener {
            startQuiz()
        }
    }

    private fun startQuiz() {
        currentQuestionIndex = 0
        score = 0
        binding.questionCard.visibility = View.VISIBLE
        binding.optionsContainer.visibility = View.VISIBLE
        binding.quizResultCard.visibility = View.GONE
        binding.quizProgress.visibility = View.VISIBLE
        binding.quizScore.visibility = View.VISIBLE

        generateQuestion()
    }

    private fun generateQuestion() {
        if (currentQuestionIndex >= totalQuestions) {
            showResults()
            return
        }

        isAnswered = false

        // Update headers
        binding.quizProgress.text = getString(R.string.quiz_progress).format(currentQuestionIndex + 1, totalQuestions)
        binding.quizScore.text = getString(R.string.quiz_score).format(score, totalQuestions)

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

        // Animate question card fade down
        binding.questionCard.alpha = 0f
        binding.questionCard.translationY = -30f
        binding.questionCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        // Bind options with staggered slide-up and scale-up overshoot animation
        for (i in optionCards.indices) {
            optionCards[i].backgroundTintList = defaultCardTint
            defaultTextColor?.let { optionTexts[i].setTextColor(it) }
            optionTexts[i].text = currentChoices[i]
            
            optionCards[i].alpha = 0f
            optionCards[i].translationY = 50f
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
    }

    private fun checkAnswer(selectedIndex: Int) {
        isAnswered = true
        val selectedAnswer = currentChoices[selectedIndex]
        val correctIndex = currentChoices.indexOf(currentCorrectAnswer)

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
        } else {
            optionCards[selectedIndex].backgroundTintList = ColorStateList.valueOf(incorrectBg)
            optionTexts[selectedIndex].setTextColor(incorrectText)
            if (correctIndex != -1) {
                optionCards[correctIndex].backgroundTintList = ColorStateList.valueOf(correctBg)
                optionTexts[correctIndex].setTextColor(correctText)
            }
        }

        // Spring scale up for the correct answer
        val correctCard = if (correctIndex != -1) optionCards[correctIndex] else null
        correctCard?.let { card ->
            card.animate()
                .scaleX(1.08f)
                .scaleY(1.08f)
                .setDuration(220)
                .setInterpolator(android.view.animation.OvershootInterpolator(2.0f))
                .withEndAction {
                    card.animate()
                        .scaleX(1.04f)
                        .scaleY(1.04f)
                        .setDuration(120)
                        .start()
                }
                .start()
        }

        // Wobble/shake animation for the incorrect selected choice
        if (selectedAnswer != currentCorrectAnswer) {
            val wrongCard = optionCards[selectedIndex]
            wrongCard.translationX = 0f
            wrongCard.animate()
                .translationX(-15f)
                .setDuration(50)
                .withEndAction {
                    wrongCard.animate()
                        .translationX(15f)
                        .setDuration(50)
                        .withEndAction {
                            wrongCard.animate()
                                .translationX(-10f)
                                .setDuration(50)
                                .withEndAction {
                                    wrongCard.animate()
                                        .translationX(10f)
                                        .setDuration(50)
                                        .withEndAction {
                                            wrongCard.animate()
                                                .translationX(0f)
                                                .setDuration(50)
                                                .start()
                                        }
                                        .start()
                                }
                                .start()
                        }
                        .start()
                }
                .start()
        }

        // Dim and shrink other unselected incorrect option cards
        for (i in optionCards.indices) {
            if (i != correctIndex && (i != selectedIndex || selectedAnswer == currentCorrectAnswer)) {
                optionCards[i].animate()
                    .alpha(0.3f)
                    .scaleX(0.92f)
                    .scaleY(0.92f)
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
        binding.questionCard.visibility = View.GONE
        binding.optionsContainer.visibility = View.GONE
        binding.quizProgress.visibility = View.GONE
        binding.quizScore.visibility = View.GONE

        binding.quizResultText.text = getString(R.string.quiz_finished).format(score, totalQuestions)
        binding.quizResultCard.visibility = View.VISIBLE
        Utils.fadeInAnim(binding.quizResultCard, 300)
    }
}
