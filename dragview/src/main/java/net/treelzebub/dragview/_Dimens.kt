package net.treelzebub.dragview

import android.view.View

/**
 * Created by Tre Murillo on 6/2/17
 */

internal fun View.pxToSp(px: Float): Float {
    return px / context.resources.displayMetrics.scaledDensity
}