package com.ai.gameautomation.service

data class Decision(
    val action: String,  // click, swipe, wait
    val x: Int = 0,
    val y: Int = 0,
    val endX: Int = 0,
    val endY: Int = 0,
    val confidence: Float = 0f
)