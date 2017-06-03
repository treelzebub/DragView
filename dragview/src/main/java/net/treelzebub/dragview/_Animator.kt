package net.treelzebub.dragview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter

/**
 * Created by Tre Murillo on 6/3/17
 */

internal fun Animator.setOnEnd(fn: (Animator) -> Unit) = apply {
    addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) = fn(animation)
    })
}