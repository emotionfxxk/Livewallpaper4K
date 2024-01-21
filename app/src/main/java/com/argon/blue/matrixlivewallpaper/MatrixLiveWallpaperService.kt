package com.argon.blue.matrixlivewallpaper

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Handler
import android.os.HandlerThread
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import kotlin.random.Random


class MatrixLiveWallpaperService  : WallpaperService(){
    override fun onCreateEngine(): WallpaperService.Engine {
        return MatrixWallpaperEngine()
    }

    private inner class MatrixWallpaperEngine : WallpaperService.Engine() {
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

        private lateinit var matrixPaint:Paint
        private var drawingHandler: Handler? = null
        private var runnable: Runnable? = null

        private var bufferBitmap: Bitmap? = null
        private var bufferCanvas: Canvas? = null

        private var screenDensity = 0f

        private fun startDraw() {
            runnable?.let {
                drawingHandler?.removeCallbacks(it)
                drawingHandler?.postDelayed(it, speedInMillion)
            }
        }
        private fun stopDraw() {
            runnable?.let { drawingHandler?.removeCallbacks(it) }
        }

        fun generateRandomString(charSet:String, length:Int) : String {
            val random = Random.Default
            val sb = StringBuilder(length)
            repeat(length) {
                sb.append(charSet[random.nextInt(charSet.length)])
            }
            return sb.toString()
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            surfaceHolder.setFormat(PixelFormat.RGBA_8888)
        }

        override fun onDestroy() {
            super.onDestroy()
        }

        override fun onSurfaceCreated(holder:SurfaceHolder) {
            super.onSurfaceCreated(holder)
            val width = holder.surfaceFrame.width()
            val height = holder.surfaceFrame.height()

            spUtil = PreferenceUtils(this@MatrixLiveWallpaperService)
            utils = Utils(this@MatrixLiveWallpaperService)
            matrixCharset = spUtil.getMatrixCharset()?.let { utils.getCharsetFromName(it) }.toString()

            screenDensity = resources.displayMetrics.density
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
            matrixColumns = ( width / chWidth).toInt()
            lenOfVerticalString = (height / matrixPaint.textSize).toInt() + 1;

            for(i in 0 until matrixColumns) {
                dropsEndIndex.add(Random.nextInt(lenOfVerticalString))
                stringMatrix.add(generateRandomString(matrixCharset, lenOfVerticalString))
            }

            val drawingThread = HandlerThread("MatrixDrawingThread")
            drawingThread.start()
            drawingHandler= Handler(drawingThread.looper)
            runnable = object : Runnable {
                override fun run() {
                    drawMatrix(surfaceHolder)
                    drawingHandler?.removeCallbacks(this)
                    drawingHandler?.postDelayed(this, speedInMillion)
                }
            }

            bufferBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bufferCanvas = Canvas(bufferBitmap!!)

            startDraw()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible);
            if (visible) {
                startDraw()
            } else {
                stopDraw()
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            stopDraw()
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder, format: Int,
            width: Int, height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
        }

        private fun drawMatrix(holder:SurfaceHolder){
            var canvas:Canvas? = null

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
                canvas = holder.lockCanvas()
                canvas.drawBitmap(bufferBitmap!!, 0f, 0f, null)
            } finally {
                canvas?.let { holder.unlockCanvasAndPost(it) }
            }
        }
    }
}