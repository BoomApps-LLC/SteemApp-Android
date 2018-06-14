package com.boomapps.steemapp.ui.qrreader

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View


/**
 * Created by Vitali Grechikha on 07.02.2018.
 */
class PointsOverlayView : View {
    constructor(ctx: Context) : super(ctx) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }

    var points: Array<PointF>? = null

    private var paint: Paint? = null

    private fun initView() {
        paint = Paint()
        paint!!.setColor(Color.YELLOW)
        paint!!.setStyle(Paint.Style.FILL)
    }

    fun setNewPoints(points: Array<PointF>) {
        this.points = points
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (points != null) {
            for (pointF in points!!) {
                canvas.drawCircle(pointF.x, pointF.y, 10f, paint)
            }
        }
    }
}