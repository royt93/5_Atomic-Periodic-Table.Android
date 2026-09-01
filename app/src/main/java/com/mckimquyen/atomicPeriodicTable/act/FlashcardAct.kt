package com.mckimquyen.atomicPeriodicTable.act

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.WindowCompat
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.databinding.AFlashcardBinding
import com.mckimquyen.atomicPeriodicTable.feature.flashcard.FlashcardPref
import com.mckimquyen.atomicPeriodicTable.feature.flashcard.FlashcardRating
import com.mckimquyen.atomicPeriodicTable.feature.flashcard.FlashcardScheduler
import com.mckimquyen.atomicPeriodicTable.feature.streak.StudyStreakPref
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.model.ElementModel
import com.mckimquyen.atomicPeriodicTable.util.ElementTranslator

class FlashcardAct : BaseAct() {

    private lateinit var binding: AFlashcardBinding
    private lateinit var pref: FlashcardPref
    private var queue: List<Element> = emptyList()
    private var currentIndex = 0
    private var isFlipped = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        binding = AFlashcardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        pref = FlashcardPref(this)
        queue = buildDueQueue()
        showCard(0, animated = false)

        binding.cardFlashcard.setOnClickListener { flipCard() }
        binding.btnFlashcardAgain.setOnClickListener { rate(FlashcardRating.AGAIN) }
        binding.btnFlashcardHard.setOnClickListener { rate(FlashcardRating.HARD) }
        binding.btnFlashcardGood.setOnClickListener { rate(FlashcardRating.GOOD) }
        binding.btnFlashcardEasy.setOnClickListener { rate(FlashcardRating.EASY) }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
        binding.flashcardBackBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    /** Cards due for review now; if nothing is due yet, fall back to the full deck (practice mode). */
    private fun buildDueQueue(): List<Element> {
        val elements = ArrayList<Element>()
        ElementModel.getList(elements)
        val now = System.currentTimeMillis()
        val due = elements.filter { FlashcardScheduler.isDue(pref.getNextReviewAtMs(it.short), now) }
        return due.ifEmpty { elements }
    }

    private fun showCard(index: Int, animated: Boolean = true) {
        if (index >= queue.size) {
            showComplete(animated)
            return
        }
        currentIndex = index
        isFlipped = false

        val card = binding.cardFlashcard
        card.rotationY = 0f

        fun bindCurrentCard() {
            val element = queue[index]
            binding.tvFlashcardFront.text = element.short
            binding.tvFlashcardBack.text = "${ElementTranslator.getLocalizedName(this, element.element)} (#${element.number})"
            binding.tvFlashcardBack.visibility = View.GONE
            binding.tvFlashcardHint.visibility = View.VISIBLE
            binding.layoutFlashcardRatings.visibility = View.INVISIBLE
            binding.tvFlashcardProgress.text = getString(R.string.flashcard_progress, index + 1, queue.size)
            binding.flashcardProgressBar.setProgressCompat((index * 100) / queue.size, animated)
            card.visibility = View.VISIBLE
            binding.cardFlashcardComplete.visibility = View.GONE
        }

        if (animated) {
            // Slide the finished card out, then slide the next one in from the other side.
            card.animate().alpha(0f).translationX(-60f).setDuration(120).withEndAction {
                bindCurrentCard()
                card.translationX = 60f
                card.animate().alpha(1f).translationX(0f).setDuration(180).start()
            }.start()
        } else {
            bindCurrentCard()
        }
    }

    /** 3D flip: rotate to 90° (edge-on, content invisible), swap front/back content, rotate back. */
    private fun flipCard() {
        if (isFlipped) return
        isFlipped = true
        val card = binding.cardFlashcard
        card.cameraDistance = 12000f * resources.displayMetrics.density
        ObjectAnimator.ofFloat(card, View.ROTATION_Y, 0f, 90f).apply {
            duration = 150
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.tvFlashcardBack.visibility = View.VISIBLE
                    binding.tvFlashcardHint.visibility = View.GONE
                    binding.layoutFlashcardRatings.visibility = View.VISIBLE
                    ObjectAnimator.ofFloat(card, View.ROTATION_Y, -90f, 0f).setDuration(150).start()
                }
            })
            start()
        }
    }

    private fun rate(rating: FlashcardRating) {
        if (!isFlipped) return
        val element = queue[currentIndex]
        val newState = FlashcardScheduler.review(pref.getState(element.short), rating)
        val nextReviewAtMs = FlashcardScheduler.nextReviewAtMs(System.currentTimeMillis(), newState.intervalDays)
        pref.saveState(element.short, newState, nextReviewAtMs)
        StudyStreakPref(this).recordStudyToday()
        showCard(currentIndex + 1)
    }

    private fun showComplete(animated: Boolean) {
        binding.cardFlashcard.visibility = View.GONE
        binding.layoutFlashcardRatings.visibility = View.INVISIBLE
        binding.cardFlashcardComplete.alpha = 0f
        binding.cardFlashcardComplete.visibility = View.VISIBLE
        binding.cardFlashcardComplete.animate().alpha(1f).setDuration(if (animated) 200 else 0).start()
        binding.tvFlashcardProgress.text = getString(R.string.flashcard_progress, queue.size, queue.size)
        binding.flashcardProgressBar.setProgressCompat(100, animated)
    }

    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        val params = binding.flashcardTitleBar.layoutParams as ViewGroup.LayoutParams
        params.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.flashcardTitleBar.layoutParams = params
        binding.flashcardRootView.setPadding(0, 0, 0, bottom)
    }
}
