package com.example.chatwithgiminibypass

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.text.Html
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewMessage: TextView = view.findViewById(R.id.textViewMessage)
        val buttonCopy: TextView = view.findViewById(R.id.buttonCopy)
        val buttonShare: TextView = view.findViewById(R.id.buttonShare)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        val context = holder.itemView.context

        val params = holder.textViewMessage.layoutParams as LinearLayout.LayoutParams
        if (message.isUser) {
            params.gravity = Gravity.END
            holder.textViewMessage.setBackgroundColor(0xFFDCF8C6.toInt()) // light green
            holder.textViewMessage.text = message.text
        } else {
            params.gravity = Gravity.START
            holder.textViewMessage.setBackgroundColor(0xFFE0E0E0.toInt()) // light gray

            if (message.isLoading) {
                holder.textViewMessage.text = "Thinking... ⏳"
            } else {
                holder.textViewMessage.text = Html.fromHtml(message.text, Html.FROM_HTML_MODE_COMPACT)
            }
        }
        holder.textViewMessage.layoutParams = params

        // Copy button click listener
        holder.buttonCopy.setOnClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Chat Message", message.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        // Share button click listener
        holder.buttonShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message.text)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Message"))
        }
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun updateLastMessage(newText: String) {
        if (messages.isNotEmpty()) {
            val lastIndex = messages.size - 1
            messages[lastIndex].text = newText
            messages[lastIndex].isLoading = false
            notifyItemChanged(lastIndex)
        }
    }

    fun removeLastMessage() {
        if (messages.isNotEmpty()) {
            val lastIndex = messages.size - 1
            messages.removeAt(lastIndex)
            notifyItemRemoved(lastIndex)
        }
    }

    fun clearMessages() {
        messages.clear()
        notifyDataSetChanged()
    }
}
