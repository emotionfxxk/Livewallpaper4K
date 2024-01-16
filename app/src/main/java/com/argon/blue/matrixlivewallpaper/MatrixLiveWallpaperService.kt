package com.argon.blue.matrixlivewallpaper

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Handler
import android.os.HandlerThread
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import kotlin.random.Random


class MatrixLiveWallpaperService  : WallpaperService(){
    override fun onCreateEngine(): WallpaperService.Engine {
        return MyWallpaperEngine()
    }

    private inner class MyWallpaperEngine : WallpaperService.Engine() {
        private var matrixCharset : String = "゠アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレワヰヱヲンヺ・ーヽヿ0123456789"
        //private var matrixCharset : String = "゠ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ0123456789"
        //private var matrixCharset : String = "゠абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ0123456789"
        //private var matrixCharset : String = "゠กขฃคฅฆงจฉชซฌญฎฏฐฑฒณดตถทธนบปผฝพฟภมยรฤลฦวศษสหฬอฮะาำเแโใไๅ็่้๊๋์0123456789"
        //private var matrixCharset : String = "鼠牛虎兔龍蛇馬羊猴雞狗豬水火木金土仁義禮智信命愛和誌氣力道喜福夢強心忠勇德禪緣壽榮幸美花富錢囍豐貴神恨死活鬼高低矮靜醜輸勝贏飛悲歡吻怒笑性煩誠忍弟姐妹妻夫父母友春夏秋冬"
        private var rainbowSpeed : Float = 1.75f
        private var rainbowOn : Boolean = true
        private var rainbowLightness: Int = 45
        private var rainbowSaturation: Int = 100
        private var speedInMillion:Long = 60
        private var matrixTextSize:Float =  12f // size in dip
        private var matrixColumns:Int = 0
        private var chWidth:Float = 0f
        private var dropsEndIndex = mutableListOf<Int>()
        private var stringMatrix  = mutableListOf<String>()
        private var lenOfVisibleString = 0
        private var lenOfVerticalString = 0

        private lateinit var matrixPaint:Paint
        private var drawingHandler: Handler? = null
        private var runnable: Runnable? = null
        private var gradient:LinearGradient ? = null
        private val matrix = Matrix()

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
            screenDensity = resources.displayMetrics.density
            matrixPaint = Paint().apply {
                color = Color.GREEN
                textSize = matrixTextSize * screenDensity
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL) // 设置字体样式
                maskFilter = BlurMaskFilter(2f, BlurMaskFilter.Blur.SOLID)
            }
            matrixPaint.setShadowLayer(40f, 2f, 2f, Color.GREEN)


            chWidth = matrixPaint?.measureText(matrixCharset.get(0).toString())!!
            matrixColumns = ( width / chWidth).toInt()
            lenOfVerticalString = (height / matrixPaint.textSize).toInt() + 1;
            lenOfVisibleString = (lenOfVerticalString / 2f).toInt()
            Log.d("GAGA", "displayMetrics H:${resources.displayMetrics.heightPixels}")

            /*
            gradient = LinearGradient(
                resources.displayMetrics.widthPixels / 2f,
                0f,
                resources.displayMetrics.widthPixels / 2f,
                lenOfVisibleString * matrixPaint.textSize,
                intArrayOf(Color.TRANSPARENT,
                    Color.parseColor("#7F00C800").toInt(),
                    Color.parseColor("#FF00FF46").toInt(),
                    Color.parseColor("#FF00FF00").toInt(),
                    Color.WHITE),
                null,
                Shader.TileMode.CLAMP
            )
            matrixPaint.shader = gradient*/
            for(i in 0 until matrixColumns) {
                dropsEndIndex.add(Random.nextInt(lenOfVerticalString))
                stringMatrix.add(generateRandomString(matrixCharset, lenOfVerticalString))
            }

            var drawingThread = HandlerThread("MatrixDrawingThread")
            drawingThread.start()
            drawingHandler= Handler(drawingThread.looper)
            runnable = object : Runnable {
                override fun run() {
                    drawMatrix(surfaceHolder)
                    drawingHandler?.removeCallbacks(this)
                    drawingHandler?.postDelayed(this, speedInMillion)
                }
            }
            Log.d("GAGA", "onSurfaceCreated  frame H:${height}")
            // 创建绘制缓冲区
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
                //canvas = holder.lockCanvas()
                bufferCanvas!!.drawColor(0x10000000)
                for (col in 0 until matrixColumns) {
                    val xPos = chWidth / 2f + chWidth * col
                    var yPos = matrixPaint.textSize / 2f
                    /*
                    gradient?.setLocalMatrix(matrix.apply {
                        setTranslate(0f, (dropsEndIndex[col] - lenOfVisibleString) * matrixPaint.textSize)
                    })*/
                    var strColumn = stringMatrix.get(col)
                    var startIndex = 0
                    var endIndex = dropsEndIndex[col]
                    if (endIndex < lenOfVisibleString ) {
                        startIndex = 0
                    } else if (endIndex >= lenOfVerticalString) {
                        startIndex = endIndex - lenOfVisibleString
                        endIndex = lenOfVerticalString - 1
                    } else {
                        startIndex = endIndex - lenOfVisibleString
                    }

                    if(endIndex > startIndex) {
                        var visibleStr = strColumn.substring(startIndex, endIndex)
                        /*
                        for (i in 0 until visibleStr.length) {
                            var ch = visibleStr[i].toString()
                            bufferCanvas!!.drawText(ch.toString(), xPos, yPos + startIndex * matrixPaint.textSize, matrixPaint)
                            yPos += matrixPaint.textSize
                        }*/
                        var ch = visibleStr[visibleStr.length - 1].toString()
                        bufferCanvas!!.drawText(ch.toString(), xPos, yPos + endIndex * matrixPaint.textSize, matrixPaint)
                        //yPos += matrixPaint.textSize
                    }
                    dropsEndIndex[col] = dropsEndIndex[col] + 1
                    if(dropsEndIndex[col] > lenOfVerticalString + lenOfVisibleString - 1) {
                        dropsEndIndex[col] = 0;
                        stringMatrix.set(col, generateRandomString(matrixCharset, lenOfVerticalString))
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