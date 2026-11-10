package com.example.chatwithgiminibypass

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.view.Gravity
import android.widget.LinearLayout
import android.text.Html

class ChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewMessage: TextView = view.findViewById(R.id.textViewMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]

        // Align user messages to the right and assistant/gemini messages to the left
        val params = holder.textViewMessage.layoutParams as LinearLayout.LayoutParams
        if (message.isUser) {
            params.gravity = Gravity.END
            holder.textViewMessage.setBackgroundColor(0xFFDCF8C6.toInt()) // light green for user
            holder.textViewMessage.text = message.text
        } else {
            params.gravity = Gravity.START
            holder.textViewMessage.setBackgroundColor(0xFFE0E0E0.toInt()) // light gray for gemini
            // Render HTML formatting (bold, italic, lists, etc.) from Gemini response
            holder.textViewMessage.text = Html.fromHtml(message.text, Html.FROM_HTML_MODE_COMPACT)
        }
        holder.textViewMessage.layoutParams = params
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
}
