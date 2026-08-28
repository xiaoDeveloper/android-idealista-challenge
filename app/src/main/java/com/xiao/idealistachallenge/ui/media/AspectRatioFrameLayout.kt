package com.xiao.idealistachallenge.ui.media

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.xiao.idealistachallenge.R

/** A lightweight XML-only media viewport that keeps its children at a fixed aspect ratio. */
class AspectRatioFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val ratio: Pair<Int, Int> = context.obtainStyledAttributes(
        attrs,
        R.styleable.AspectRatioFrameLayout,
        defStyleAttr,
        0,
    ).let { attributes ->
        try {
            attributes.getString(R.styleable.AspectRatioFrameLayout_aspectRatio)
                .toAspectRatioOrDefault()
        } finally {
            attributes.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val availableWidth = MeasureSpec.getSize(widthMeasureSpec)
        if (availableWidth == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        val height = availableWidth * ratio.second / ratio.first
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY),
        )
    }
}

private fun String?.toAspectRatioOrDefault(): Pair<Int, Int> {
    val parts = this?.split(':') ?: return DEFAULT_RATIO
    val width = parts.getOrNull(0)?.toIntOrNull()
    val height = parts.getOrNull(1)?.toIntOrNull()
    return if (width == null || height == null || width <= 0 || height <= 0) DEFAULT_RATIO
    else width to height
}

private val DEFAULT_RATIO = 16 to 9
