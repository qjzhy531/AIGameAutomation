package com.ai.gameautomation.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.ai.gameautomation.R
import com.ai.gameautomation.ai.AIEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AutomationService : LifecycleService() {
    
    private var windowManager: WindowManager? = null
    private var overlayView: android.view.View? = null
    private var automationJob: Job? = null
    private var isRunning = false
    
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
            // 使用MediaProjection API捕获屏幕
            // 这里简化实现，实际需要处理MediaProjection
            Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
        }
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