package com.argon.blue.matrixlivewallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.View
import kotlin.random.Random

class MatrixRenderer {
    private lateinit var spUtil:PreferenceUtils
    private lateinit var utils:Utils
    private lateinit var matrixCharset : String

    private var speedInMillion:Long = 60
    private var matrixTextSize:Float =  10f // size in dip
    private var matrixColumns:Int = 0
    private var chWidth:Float = 0f
    private var dropsEndIndex = mutableListOf<Int>()
    private var stringMatrix  = mutableListOf<String>()
    private var lenOfVerticalString = 0
    private var isRendering = false
    private var isInitialized = false


    private lateinit var matrixPaint: Paint
    private var drawingHandler: Handler? = null
    private var runnable: Runnable? = null

    private var bufferBitmap: Bitmap? = null
    private var bufferCanvas: Canvas? = null

    private var screenDensity = 0f
    private var mWidth = 0
    private var mHeight = 0
    private var myView:View?=null
    fun startRendering() {
        if (!isRendering && isInitialized) {
            isRendering = true
            runnable?.let {
                drawingHandler?.removeCallbacks(it)
                drawingHandler?.postDelayed(it, speedInMillion)
            }
        }
    }
    fun stopRendering() {
        if (isRendering) {
            runnable?.let { drawingHandler?.removeCallbacks(it) }
            isRendering = false
        }
    }

    private fun generateRandomString(charSet:String, length:Int) : String {
        val random = Random.Default
        val sb = StringBuilder(length)
        repeat(length) {
            sb.append(charSet[random.nextInt(charSet.length)])
        }
        return sb.toString()
    }
    fun updateCharset() {
        if (!isInitialized) return
        stopRendering()
        matrixCharset = spUtil.getMatrixCharset()?.let { utils.getCharsetFromName(it) }.toString()
        chWidth = matrixPaint?.measureText(matrixCharset.get(0).toString())!!
        matrixColumns = ( mWidth / chWidth).toInt()
        lenOfVerticalString = (mHeight / matrixPaint.textSize).toInt() + 1;

        dropsEndIndex.clear()
        stringMatrix.clear()
        for(i in 0 until matrixColumns) {
            dropsEndIndex.add(Random.nextInt(lenOfVerticalString))
            stringMatrix.add(generateRandomString(matrixCharset, lenOfVerticalString))
        }
        startRendering()
    }
    fun updateTextColor() {
        if (!isInitialized) return
        stopRendering()
        matrixPaint = Paint().apply {
            color = spUtil.getMatrixTextColor()
            textSize = matrixTextSize * screenDensity
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL) // 设置字体样式
            maskFilter = BlurMaskFilter(4f, BlurMaskFilter.Blur.SOLID)
        }
        matrixPaint.setShadowLayer(40f, 2f, 2f, spUtil.getMatrixTextColor())
        startRendering()
    }
    fun initRenderer(view:View, context: Context, width:Int, height:Int) {
        if (isInitialized) return
        myView = view
        isInitialized = true
        mWidth = width
        mHeight = height
        spUtil = PreferenceUtils(context)
        utils = Utils(context)

        matrixCharset = spUtil.getMatrixCharset()?.let { utils.getCharsetFromName(it) }.toString()

        screenDensity = context.resources.displayMetrics.density
        matrixPaint = Paint().apply {
            color = spUtil.getMatrixTextColor()
            textSize = matrixTextSize * screenDensity
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL) // 设置字体样式
            maskFilter = BlurMaskFilter(4f, BlurMaskFilter.Blur.SOLID)
        }
        matrixPaint.setShadowLayer(40f, 2f, 2f, spUtil.getMatrixTextColor())


        chWidth = matrixPaint?.measureText(matrixCharset.get(0).toString())!!
        matrixColumns = ( mWidth / chWidth).toInt()
        lenOfVerticalString = (mHeight / matrixPaint.textSize).toInt() + 1;

        for(i in 0 until matrixColumns) {
            dropsEndIndex.add(Random.nextInt(lenOfVerticalString))
            stringMatrix.add(generateRandomString(matrixCharset, lenOfVerticalString))
        }

        val drawingThread = HandlerThread("MatrixPreviewThread")
        drawingThread.start()
        drawingHandler= Handler(drawingThread.looper)
        runnable = object : Runnable {
            override fun run() {
                //Log.d("GAGA", "runnable")
                myView!!.postInvalidate()
                drawingHandler?.removeCallbacks(this)
                drawingHandler?.postDelayed(this, speedInMillion)
            }
        }

        bufferBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bufferCanvas = Canvas(bufferBitmap!!)
    }

    fun drawMatrixFrame(canvas: Canvas){
        try {
            bufferCanvas!!.drawColor(0x10000000)
            for (col in 0 until matrixColumns) {
                val xPos = chWidth / 2f + chWidth * col
                var yPos = matrixPaint.textSize / 2f
                val strColumn = stringMatrix[col]
                val endIndex = dropsEndIndex[col]
                bufferCanvas!!.drawText(strColumn.get(endIndex).toString(), xPos, yPos + endIndex * matrixPaint.textSize, matrixPaint)

                dropsEndIndex[col] = dropsEndIndex[col] + 1
                if(dropsEndIndex[col] > lenOfVerticalString -1) {
                    dropsEndIndex[col] = 0
                    stringMatrix[col] = generateRandomString(matrixCharset, lenOfVerticalString)
                }
            }
            canvas.drawBitmap(bufferBitmap!!, 0f, 0f, null)
        } finally {
        }
    }
}