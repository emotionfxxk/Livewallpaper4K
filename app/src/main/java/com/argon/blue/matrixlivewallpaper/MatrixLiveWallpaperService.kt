package com.argon.blue.matrixlivewallpaper

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Handler
import android.os.HandlerThread
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import kotlin.random.Random


class MatrixLiveWallpaperService  : WallpaperService(){
    override fun onCreateEngine(): WallpaperService.Engine {
        return MatrixWallpaperEngine()
    }

    private inner class MatrixWallpaperEngine : WallpaperService.Engine(), OnSharedPreferenceChangeListener{
        private lateinit var spUtil:PreferenceUtils
        private lateinit var utils:Utils
        private lateinit var matrixCharset : String

        private var speedInMillion:Long = 60
        private var matrixTextSize:Float =  10f // size in dip
        private var matrixColumns:Int = 0
        private var chWidth:Float = 0f
        private var dropsEndIndex = mutableListOf<Int>()
        private var stringMatrix  = mutableListOf<String>()
        val lock = Any()

        private var lenOfVerticalString = 0

        private lateinit var matrixPaint:Paint
        private var drawingHandler: Handler? = null
        private var runnable: Runnable? = null

        private var bufferBitmap: Bitmap? = null
        private var bufferCanvas: Canvas? = null
        private var mWidth = 0
        private var mHeight = 0

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

        override fun onSurfaceCreated(holder:SurfaceHolder) {
            super.onSurfaceCreated(holder)
            mWidth = holder.surfaceFrame.width()
            mHeight = holder.surfaceFrame.height()

            spUtil = PreferenceUtils(this@MatrixLiveWallpaperService)
            spUtil.registerListener(this@MatrixWallpaperEngine)
            utils = Utils(this@MatrixLiveWallpaperService)
            matrixCharset =
                spUtil.getMatrixCharset()?.let { utils.getCharsetFromName(it) }.toString()

            screenDensity = resources.displayMetrics.density
            matrixPaint = Paint().apply {
                color = spUtil.getMatrixTextColor()
                textSize = matrixTextSize * screenDensity
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL) // 设置字体样式
                maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.SOLID)
            }
            matrixPaint.setShadowLayer(40f, 4f, 4f, spUtil.getMatrixTextColor())

            chWidth = matrixPaint?.measureText(matrixCharset[0].toString())!!
            matrixColumns = (mWidth / chWidth).toInt()
            lenOfVerticalString = (mHeight / matrixPaint.textSize).toInt() + 1;

            synchronized(lock) {
                for (i in 0 until matrixColumns) {
                    dropsEndIndex.add(Random.nextInt(lenOfVerticalString))
                    stringMatrix.add(generateRandomString(matrixCharset, lenOfVerticalString))
                }
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

            bufferBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
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
                synchronized(lock) {
                    for (col in 0 until matrixColumns) {
                        val xPos = chWidth / 2f + chWidth * col
                        val yPos = matrixPaint.textSize / 2f
                        val strColumn = stringMatrix[col]
                        val endIndex = dropsEndIndex[col]
                        bufferCanvas!!.drawText(
                            strColumn[endIndex].toString(),
                            xPos,
                            yPos + endIndex * matrixPaint.textSize,
                            matrixPaint
                        )
                        //bufferCanvas!!.drawText(strColumn.get(endIndex).toString(), xPos+4, yPos + endIndex * matrixPaint.textSize+4, matrixPaintBlur)
                        dropsEndIndex[col] = dropsEndIndex[col] + 1
                        if (dropsEndIndex[col] > lenOfVerticalString - 1) {
                            dropsEndIndex[col] = 0
                            stringMatrix[col] =
                                generateRandomString(matrixCharset, lenOfVerticalString)
                        }
                    }
                }
                canvas = holder.lockCanvas()
                canvas?.drawBitmap(bufferBitmap!!, 0f, 0f, null)
            } finally {
                canvas?.let {
                    if (holder.surface.isValid) {
                        holder.unlockCanvasAndPost(it)
                    }
                }
            }
        }

        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences?,
            key: String?
        ) {
            stopDraw()
            matrixCharset =
                spUtil.getMatrixCharset()?.let { utils.getCharsetFromName(it) }.toString()
            chWidth = matrixPaint?.measureText(matrixCharset[0].toString())!!
            matrixColumns = (mWidth / chWidth).toInt()
            lenOfVerticalString = (mHeight / matrixPaint.textSize).toInt() + 1;

            synchronized(lock) {
                dropsEndIndex.clear()
                stringMatrix.clear()
                for (i in 0 until matrixColumns) {
                    dropsEndIndex.add(Random.nextInt(lenOfVerticalString))
                    stringMatrix.add(generateRandomString(matrixCharset, lenOfVerticalString))
                }
            }
            matrixPaint.color = spUtil.getMatrixTextColor()
            startDraw()
        }
    }
}