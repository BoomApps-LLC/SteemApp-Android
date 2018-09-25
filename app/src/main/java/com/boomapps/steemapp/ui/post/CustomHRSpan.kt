package com.boomapps.steemapp.ui.post

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.style.LineBackgroundSpan
import android.text.style.LineHeightSpan


class CustomHRSpan(val color: Int, val height: Float, val line: Float) : LineBackgroundSpan, LineHeightSpan {

    private val marginLeft = 5.0f   // Margin of line, left side
    private val marginRight = 5.0f  // Margin of line, right side

    override fun drawBackground(c: Canvas, p: Paint, left: Int, right: Int, top: Int, baseline: Int, bottom: Int, text: CharSequence?, start: Int, end: Int, lnum: Int) {
        val paintColor = p.color
        val y = (top + (bottom - top) / 2) as Float - line * 0.5f
        val r = RectF((left.toFloat()) + marginLeft, y,
                ((right - left).toFloat() - marginRight), y + line)
        p.color = color
        c.drawRect(r, p)
        p.color = paintColor
    }

    override fun chooseHeight(text: CharSequence?, start: Int, end: Int, spanstartv: Int, v: Int, fm: Paint.FontMetricsInt) {
        fm.descent = height.toInt() / 2
        fm.ascent = height.toInt() - fm.descent
        fm.leading = 0
        fm.top = fm.ascent
        fm.bottom = fm.descent
    }
}