package com.ai.gameautomation.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi

class GameAccessibilityService : AccessibilityService() {
    
    companion object {
        private var instance: GameAccessibilityService? = null
        
        fun performClick(x: Int, y: Int) {
            instance?.dispatchClick(x, y)
        }
        
        fun performSwipe(x1: Int, y1: Int, x2: Int, y2: Int) {
            instance?.dispatchSwipe(x1, y1, x2, y2)
        }
        
        fun getCurrentScreenInfo(): String {
            return instance?.getScreenInfo() ?: ""
        }
    }
    
    private fun dispatchClick(x: Int, y: Int) {
        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())
        
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 100))
        
        dispatchGesture(gestureBuilder.build(), null, null)
    }
    
    private fun dispatchSwipe(x1: Int, y1: Int, x2: Int, y2: Int) {
        val path = Path()
        path.moveTo(x1.toFloat(), y1.toFloat())
        path.lineTo(x2.toFloat(), y2.toFloat())
        
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 300))
        
        dispatchGesture(gestureBuilder.build(), null, null)
    }
    
    private fun getScreenInfo(): String {
        val rootNode = rootInActiveWindow ?: return "No root node"
        
        val info = StringBuilder()
        info.append("Screen Elements:\n")
        
        // 获取所有可点击的元素
        val clickableNodes = findClickableNodes(rootNode)
        for (node in clickableNodes) {
            val bounds = Rect()
            node.getBoundsInScreen(bounds)
            info.append("Element: ${node.text ?: node.contentDescription ?: "Unknown"}\n")
            info.append("  Position: (${bounds.left}, ${bounds.top})\n")
            info.append("  Size: ${bounds.width()}x${bounds.height()}\n")
        }
        
        return info.toString()
    }
    
    private fun findClickableNodes(node: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()
        
        if (node.isClickable) {
            result.add(node)
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            result.addAll(findClickableNodes(child))
        }
        
        return result
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 监听屏幕变化
        event?.let {
            when (it.eventType) {
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    // 屏幕内容变化，可以触发AI分析
                }
            }
        }
    }
    
    override fun onInterrupt() {
        // 服务中断
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        
        // 配置服务
        val info = serviceInfo
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        info.notificationTimeout = 100
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}