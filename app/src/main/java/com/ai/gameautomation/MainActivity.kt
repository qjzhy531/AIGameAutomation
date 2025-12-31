package com.ai.gameautomation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ai.gameautomation.service.AutomationService

class MainActivity : AppCompatActivity() {
    
    private lateinit var tvStatus: TextView
    private lateinit var switchAutomation: Switch
    private lateinit var btnStartGame: Button
    private lateinit var btnStopGame: Button
    
    private val REQUEST_CODE_OVERLAY = 1001
    private val REQUEST_CODE_ACCESSIBILITY = 1002
    private val REQUEST_CODE_SCREEN_CAPTURE = 1003
    
    private var screenCaptureResultCode: Int = -1
    private var screenCaptureData: Intent? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        setupListeners()
        checkPermissions()
    }
    
    private fun initViews() {
        tvStatus = findViewById(R.id.tvStatus)
        switchAutomation = findViewById(R.id.switchAutomation)
        btnStartGame = findViewById(R.id.btnStartGame)
        btnStopGame = findViewById(R.id.btnStopGame)
    }
    
    private fun setupListeners() {
        switchAutomation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requestOverlayPermission()
            } else {
                stopAutomation()
            }
        }
        
        btnStartGame.setOnClickListener {
            startGameAutomation()
        }
        
        btnStopGame.setOnClickListener {
            stopGameAutomation()
        }
    }
    
    private fun checkPermissions() {
        // 检查悬浮窗权限
        if (!Settings.canDrawOverlays(this)) {
            tvStatus.text = "需要悬浮窗权限"
            switchAutomation.isEnabled = false
        }
        
        // 检查无障碍服务权限
        val accessibilityEnabled = isAccessibilityServiceEnabled()
        if (!accessibilityEnabled) {
            tvStatus.text = "需要开启无障碍服务"
        }
    }
    
    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, REQUEST_CODE_OVERLAY)
            } else {
                requestAccessibilityPermission()
            }
        }
    }
    
    private fun requestAccessibilityPermission() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivityForResult(intent, REQUEST_CODE_ACCESSIBILITY)
    }
    
    private fun requestScreenCapturePermission() {
        val mProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val captureIntent = mProjectionManager.createScreenCaptureIntent()
        startActivityForResult(captureIntent, REQUEST_CODE_SCREEN_CAPTURE)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        when (requestCode) {
            REQUEST_CODE_OVERLAY -> {
                if (Settings.canDrawOverlays(this)) {
                    requestAccessibilityPermission()
                } else {
                    switchAutomation.isChecked = false
                    Toast.makeText(this, "需要悬浮窗权限", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_CODE_ACCESSIBILITY -> {
                if (isAccessibilityServiceEnabled()) {
                    requestScreenCapturePermission()
                } else {
                    switchAutomation.isChecked = false
                    Toast.makeText(this, "需要开启无障碍服务", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_CODE_SCREEN_CAPTURE -> {
                if (resultCode == RESULT_OK && data != null) {
                    screenCaptureResultCode = resultCode
                    screenCaptureData = data
                    startAutomation()
                } else {
                    switchAutomation.isChecked = false
                    Toast.makeText(this, "需要屏幕捕获权限", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun isAccessibilityServiceEnabled(): Boolean {
        val service = getString(R.string.accessibility_service_id)
        val settings = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return settings?.contains(service) == true
    }
    
    private fun startAutomation() {
        val intent = Intent(this, AutomationService::class.java)
        intent.putExtra("resultCode", screenCaptureResultCode)
        intent.putExtra("data", screenCaptureData)
        startService(intent)
        tvStatus.text = "自动化服务已启动"
        Toast.makeText(this, "自动化服务已启动", Toast.LENGTH_SHORT).show()
    }
    
    private fun stopAutomation() {
        val intent = Intent(this, AutomationService::class.java)
        stopService(intent)
        tvStatus.text = "自动化服务已停止"
        Toast.makeText(this, "自动化服务已停止", Toast.LENGTH_SHORT).show()
    }
    
    private fun startGameAutomation() {
        // 发送开始游戏自动化的广播
        val intent = Intent("com.ai.gameautomation.START_GAME")
        sendBroadcast(intent)
        tvStatus.text = "游戏自动化已启动"
        Toast.makeText(this, "游戏自动化已启动", Toast.LENGTH_SHORT).show()
    }
    
    private fun stopGameAutomation() {
        // 发送停止游戏自动化的广播
        val intent = Intent("com.ai.gameautomation.STOP_GAME")
        sendBroadcast(intent)
        tvStatus.text = "游戏自动化已停止"
        Toast.makeText(this, "游戏自动化已停止", Toast.LENGTH_SHORT).show()
    }
}