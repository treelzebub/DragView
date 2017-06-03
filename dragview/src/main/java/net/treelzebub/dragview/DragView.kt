/**
 * MIT License
 *
 * Copyright (c) 2017 Tre Murillo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.treelzebub.dragview

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

/**
 * Created by Tre Murillo on 6/2/17
 */
class DragView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr, defStyleRes) {

    interface OnStateChangedListener {
        fun onDisabled()
        fun onEnabled()
    }

    var listener: OnStateChangedListener? = null

    private var active = false
    private var initialX = 0f
    private var initialButtonWidth = 0

    private val dragButton = ImageView(context)
    private val centerText = TextView(context).apply { gravity = Gravity.CENTER }

    private lateinit var disabledDrawable: Drawable
    private lateinit var enabledDrawable: Drawable

    private val touchListener = View.OnTouchListener {
        _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> true
            MotionEvent.ACTION_MOVE -> {
                if (initialX == 0f) {
                    initialX = dragButton.x
                }
                // TODO extract this predicate into at least one val
                if (event.x > initialX + dragButton.width / 2 && event.x + dragButton.width / 2 < width) {
                    dragButton.x = event.x - dragButton.width / 2
                }
                centerText.alpha = 1 - 1.3f * (dragButton.x + dragButton.width) / width
                true
            }
            MotionEvent.ACTION_UP -> {
                if (active) {
                    collapseButton()
                } else {
                    initialButtonWidth = dragButton.width
                    // TODO extract this predicate into at least one val
                    if (dragButton.x + dragButton.width > width * 0.85) {
                        expandButton()
                    } else {
                        moveButtonBack()
                    }
                }
                true
            }
            else -> false
        }
    }

    init {
        val background = RelativeLayout(context)
        val layoutParamsView = RelativeLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        }
        addView(background, layoutParamsView)

        val bgParams = RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        }
        background.addView(centerText, bgParams)

        val buttonParams = RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE)
            addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)
        }
        addView(dragButton, buttonParams)

        if (attrs != null && defStyleAttr == 0 && defStyleRes == 0) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.DragView)

            val drawable = ta.getDrawable(R.styleable.DragView_inner_text_background)
            background.background = drawable ?: ContextCompat.getDrawable(context, R.drawable.shape_rounded)

            centerText.text = ta.getText(R.styleable.DragView_inner_text)
            centerText.setTextColor(ta.getColor(R.styleable.DragView_inner_text_color, Color.WHITE))

            val textSize = pxToSp(ta.getDimension(R.styleable.DragView_inner_text_size, 0f))
            centerText.textSize = if (textSize == 0f) 12f else textSize

            disabledDrawable = ta.getDrawable(R.styleable.DragView_button_disabled)
            enabledDrawable  = ta.getDrawable(R.styleable.DragView_button_enabled)

            val innerTextPaddingLeft   = ta.getDimension(R.styleable.DragView_inner_text_paddingLeft, 0f)
            val innerTextPaddingTop    = ta.getDimension(R.styleable.DragView_inner_text_paddingTop, 0f)
            val innerTextPaddingRight  = ta.getDimension(R.styleable.DragView_inner_text_paddingRight, 0f)
            val innerTextPaddingBottom = ta.getDimension(R.styleable.DragView_inner_text_paddingBottom, 0f)

            centerText.setPadding(innerTextPaddingLeft.toInt(),
                                  innerTextPaddingTop.toInt(),
                                  innerTextPaddingRight.toInt(),
                                  innerTextPaddingBottom.toInt())

            val buttonBackground = ta.getDrawable(R.styleable.DragView_button_background)
            dragButton.background = buttonBackground ?: ContextCompat.getDrawable(context, R.drawable.shape_button)

            val buttonPadding       = ta.getDimension(R.styleable.DragView_button_padding, -1f).toInt()
            val buttonPaddingLeft   = ta.getDimension(R.styleable.DragView_button_paddingLeft, 0f).toInt()
            val buttonPaddingTop    = ta.getDimension(R.styleable.DragView_button_paddingTop, 0f).toInt()
            val buttonPaddingRight  = ta.getDimension(R.styleable.DragView_button_paddingRight, 0f).toInt()
            val buttonPaddingBottom = ta.getDimension(R.styleable.DragView_button_paddingBottom, 0f).toInt()
            dragButton.setImageDrawable(disabledDrawable)
            if (buttonPadding != -1) {
                dragButton.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding)
            } else {
                dragButton.setPadding(buttonPaddingLeft, buttonPaddingTop, buttonPaddingRight, buttonPaddingBottom)
            }
            ta.recycle()
        }
        setOnTouchListener(touchListener)
    }

    private fun expandButton() {
        val positionAnimator = ValueAnimator.ofFloat(dragButton.x, 0f)
        positionAnimator.addUpdateListener {
            dragButton.x = positionAnimator.animatedValue as Float
        }
        val widthAnimator = ValueAnimator.ofInt(dragButton.width, width)
        widthAnimator.addUpdateListener {
            dragButton.layoutParams = dragButton.layoutParams.apply {
                width = widthAnimator.animatedValue as Int
            }
        }

        AnimatorSet().apply {
            setOnEnd {
                active = true
                dragButton.setImageDrawable(enabledDrawable)
                listener?.onEnabled()
            }
            playTogether(positionAnimator, widthAnimator)
            start()
        }
    }

    private fun moveButtonBack() {
        val positionAnimator = ValueAnimator.ofFloat(dragButton.x, 0f)
        positionAnimator.interpolator = AccelerateDecelerateInterpolator()
        positionAnimator.addUpdateListener {
            val x = positionAnimator.animatedValue as Float
            dragButton.x = x
        }
        val objectAnimator = ObjectAnimator.ofFloat(centerText, "alpha", 1f)
        positionAnimator.duration = 200

        AnimatorSet().apply {
            playTogether(objectAnimator, positionAnimator)
            start()
        }
    }

    private fun collapseButton() {
        val widthAnimator = ValueAnimator.ofInt(dragButton.width, initialButtonWidth)
        widthAnimator.addUpdateListener {
            dragButton.layoutParams = dragButton.layoutParams.apply {
                width = widthAnimator.animatedValue as Int
            }
        }
        widthAnimator.setOnEnd {
            active = false
            dragButton.setImageDrawable(disabledDrawable)
            listener?.onDisabled()
        }
        val objectAnimator = ObjectAnimator.ofFloat(centerText, "alpha", 1f)

        AnimatorSet().apply {
            playTogether(objectAnimator, widthAnimator)
            start()
        }
    }
}