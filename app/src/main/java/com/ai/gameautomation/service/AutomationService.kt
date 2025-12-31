package com.ai.gameautomation.service

import android.app.Activity
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.ai.gameautomation.R
import com.ai.gameautomation.ai.AIEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

class AutomationService : LifecycleService() {
    
    private var windowManager: WindowManager? = null
    private var overlayView: android.view.View? = null
    private var automationJob: Job? = null
    private var isRunning = false
    
    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var screenDensity: Int = 0
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var latestScreenshot: Bitmap? = null
    private val handler = Handler(Looper.getMainLooper())
    
    private val gameReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.ai.gameautomation.START_GAME" -> startGameAutomation()
                "com.ai.gameautomation.STOP_GAME" -> stopGameAutomation()
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        initOverlay()
        registerReceiver()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra("resultCode", -1) ?: -1
        val data = intent?.getParcelableExtra<Intent>("data")
        
        if (resultCode == Activity.RESULT_OK && data != null) {
            startScreenCapture(resultCode, data)
        }
        
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopGameAutomation()
        removeOverlay()
        unregisterReceiver()
    }
    
    override fun onBind(intent: Intent): IBinder? {
        return null
    }
    
    private fun initOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 100
        params.y = 100
        
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_control, null)
        
        val btnClose = overlayView?.findViewById<Button>(R.id.btnClose)
        btnClose?.setOnClickListener {
            stopSelf()
        }
        
        windowManager?.addView(overlayView, params)
    }
    
    private fun removeOverlay() {
        overlayView?.let {
            windowManager?.removeView(it)
        }
        overlayView = null
    }
    
    private fun registerReceiver() {
        val filter = IntentFilter().apply {
            addAction("com.ai.gameautomation.START_GAME")
            addAction("com.ai.gameautomation.STOP_GAME")
        }
        registerReceiver(gameReceiver, filter)
    }
    
    private fun unregisterReceiver() {
        try {
            unregisterReceiver(gameReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun startGameAutomation() {
        if (isRunning) return
        
        isRunning = true
        automationJob = lifecycleScope.launch {
            while (isRunning) {
                try {
                    // 1. 捕获屏幕
                    val screenshot = captureScreen()
                    
                    // 2. AI分析屏幕
                    val decision = withContext(Dispatchers.Default) {
                        AIEngine.analyzeScreen(screenshot)
                    }
                    
                    // 3. 执行决策
                    executeDecision(decision)
                    
                    // 4. 延迟
                    delay(1000) // 1秒后再次循环
                } catch (e: Exception) {
                    e.printStackTrace()
                    delay(5000) // 出错后等待5秒
                }
            }
        }
    }
    
    private fun stopGameAutomation() {
        isRunning = false
        automationJob?.cancel()
        automationJob = null
    }
    
    private suspend fun captureScreen(): Bitmap {
        return withContext(Dispatchers.IO) {
            if (latestScreenshot != null) {
                return@withContext latestScreenshot!!
            }
            
            // 如果没有屏幕捕获，返回空白的 Bitmap
            Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
        }
    }
    
    private fun startScreenCapture(resultCode: Int, data: Intent?) {
        if (data == null) return
        
        val mProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        
        val metrics = DisplayMetrics()
        windowManager?.defaultDisplay?.getMetrics(metrics)
        screenDensity = metrics.densityDpi
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        
        imageReader = ImageReader.newInstance(screenWidth, screenHeight, android.graphics.PixelFormat.RGBA_8888, 2)
        
        mediaProjection = mProjectionManager.getMediaProjection(resultCode, data)
        
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            screenWidth,
            screenHeight,
            screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            null
        )
        
        imageReader?.setOnImageAvailableListener({ reader ->
            val image: Image? = reader.acquireLatestImage()
            if (image != null) {
                val planes = image.planes
                val buffer: ByteBuffer = planes[0].buffer
                val pixelStride = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * screenWidth
                
                val bitmap = Bitmap.createBitmap(
                    screenWidth + rowPadding / pixelStride,
                    screenHeight,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)
                
                latestScreenshot = Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight)
                image.close()
            }
        }, handler)
    }
    
    private fun executeDecision(decision: Decision) {
        when (decision.action) {
            "click" -> performClick(decision.x, decision.y)
            "swipe" -> performSwipe(decision.x, decision.y, decision.endX, decision.endY)
            "wait" -> {
                // 等待
            }
        }
    }
    
    private fun performClick(x: Int, y: Int) {
        // 使用无障碍服务执行点击
        GameAccessibilityService.performClick(x, y)
    }
    
    private fun performSwipe(x1: Int, y1: Int, x2: Int, y2: Int) {
        // 使用无障碍服务执行滑动
        GameAccessibilityService.performSwipe(x1, y1, x2, y2)
    }
}