package com.example.chatwithgiminibypass

data class ChatMessage(
    var text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    var isLoading: Boolean = false
)
