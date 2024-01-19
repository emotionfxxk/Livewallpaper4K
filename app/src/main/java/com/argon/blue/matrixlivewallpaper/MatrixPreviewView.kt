package com.argon.blue.matrixlivewallpaper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.View


class MatrixPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var renderer: MatrixRenderer = MatrixRenderer()
    private var viewWidth = 0
    private var viewHeight = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // 获取测量宽度和测量高度
        val measuredWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measuredHeight = MeasureSpec.getSize(heightMeasureSpec)

        // 在此处理 View 尺寸
        if (viewWidth == 0 && viewHeight == 0) {
            // 第一次调用 onMeasure()，获取 View 的尺寸
            viewWidth = measuredWidth
            viewHeight = measuredHeight

            // 执行相关操作
            renderer.initRenderer(this, context, viewWidth, viewHeight)
            renderer.startRendering()
            Log.d("GAGA", "onMeasure ${viewWidth}, ${viewHeight}")
        }

        // 设置 View 的尺寸规格
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE) {
            renderer.startRendering()
        } else {
            renderer.stopRendering()
        }
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        renderer.drawMatrixFrame(canvas)
    }
}