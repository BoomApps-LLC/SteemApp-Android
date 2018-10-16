package com.boomapps.steemapp.ui.post

import android.graphics.Rect
import ru.noties.markwon.renderer.ImageSize
import ru.noties.markwon.renderer.ImageSizeResolver
import timber.log.Timber

class SteemImageSizeResolver() : ImageSizeResolver() {

    private val UNIT_PERCENT = "%"
    private val UNIT_EM = "em"
    private val widthCriteria = 0.5f

    override fun resolveImageSize(imageSize: ImageSize?, imageBounds: Rect, canvasWidth: Int, textSize: Float): Rect {
        if (imageSize == null) {
            // @since 2.0.0 post process bounds to fit canvasWidth (previously was inside AsyncDrawable)
            //      must be applied only if imageSize is null
            Timber.d("resolveImageSize::imageSize=[${imageBounds.width()}; ${imageBounds.height()}] && canvasWidth=$canvasWidth")
            val rect: Rect
            val w = imageBounds.width()
            if (w > canvasWidth) {
                Timber.d("resolveImageSize:: w > canvasWidth")
                val reduceRatio = w.toFloat() / canvasWidth
                rect = Rect(
                        0,
                        0,
                        canvasWidth,
                        (imageBounds.height() / reduceRatio + .5f).toInt()
                )

            } else {
                Timber.d("resolveImageSize:: w <= canvasWidth")
                // increase to 0.75 of width
                if (w.toFloat() > canvasWidth * widthCriteria) {
                    Timber.d("resolveImageSize:: w > canvasWidth * widthCriteria")
                    rect = imageBounds
                } else {
                    Timber.d("resolveImageSize:: w <= canvasWidth * widthCriteria")
                    val increaseRation = canvasWidth * widthCriteria / w.toFloat()
                    rect = Rect(
                            0,
                            0,
                            (canvasWidth * widthCriteria).toInt(),
                            (imageBounds.height() * increaseRation + .5f).toInt()
                    )
                }

            }
            Timber.d("resolveImageSize:: resultRect >> [${rect.right}; ${rect.bottom}]")
            return rect
        }

        val rect: Rect

        val width = imageSize.width
        val height = imageSize.height

        val imageWidth = imageBounds.width()
        val imageHeight = imageBounds.height()

        val ratio = imageWidth.toFloat() / imageHeight

        if (width != null) {

            val w: Int
            val h: Int

            if (UNIT_PERCENT == width.unit) {
                w = (canvasWidth * (width.value / 100f) + .5f).toInt()
            } else {
                w = resolveAbsolute(width, imageWidth, textSize)
            }

            if (height == null || UNIT_PERCENT == height.unit) {
                h = (w / ratio + .5f).toInt()
            } else {
                h = resolveAbsolute(height, imageHeight, textSize)
            }

            rect = Rect(0, 0, w, h)

        } else if (height != null) {

            if (UNIT_PERCENT != height.unit) {
                val h = resolveAbsolute(height, imageHeight, textSize)
                val w = (h * ratio + .5f).toInt()
                rect = Rect(0, 0, w, h)
            } else {
                rect = imageBounds
            }
        } else {
            rect = imageBounds
        }

        return rect

    }


    protected fun resolveAbsolute(dimension: ImageSize.Dimension, original: Int, textSize: Float): Int {
        val out: Int
        if (UNIT_EM == dimension.unit) {
            out = (dimension.value * textSize + .5f).toInt()
        } else {
            out = (dimension.value + .5f).toInt()
        }
        return out
    }

}