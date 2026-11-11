package com.example.chatwithgiminibypass

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatwithgiminibypass.databinding.ActivityMainBinding
import com.example.chatwithgiminibypass.db.ChatDatabaseHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessage>()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var dbHelper: ChatDatabaseHelper

    private var currentProvider: AiProvider = AiProvider.GEMINI

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                maxOf(systemBars.bottom, imeInsets.bottom)
            )
            insets
        }

        dbHelper = ChatDatabaseHelper(this)

        chatAdapter = ChatAdapter(messageList)
        binding.recyclerViewMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        binding.recyclerViewMessages.adapter = chatAdapter

        // Load saved chat history from SQLite Database
        loadChatHistory()

        // Configure Provider Spinner
        val providers = AiProvider.entries
        val providerNames = providers.map { it.displayName }
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, providerNames)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerProvider.adapter = spinnerAdapter

        binding.spinnerProvider.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = providers[position]
                if (selected != currentProvider) {
                    currentProvider = selected
                    loadProviderUrl(currentProvider.url)
                    Toast.makeText(this@MainActivity, "Switched to ${currentProvider.displayName}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Configure background WebView for AI web app
        setupWebView()

        // Load default provider URL
        loadProviderUrl(currentProvider.url)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.webViewGemini.canGoBack()) {
                    binding.webViewGemini.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        // Clear Chat & Reset Session with Random User-Agent
        binding.buttonClearChat.setOnClickListener {
            clearChatAndResetSession()
        }

        // Send Button
        binding.buttonSend.setOnClickListener {
            val text = binding.editTextMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                // 1. Add user message to UI & DB
                val userMessage = ChatMessage(text, isUser = true)
                chatAdapter.addMessage(userMessage)
                dbHelper.insertMessage(userMessage, currentProvider.displayName)
                binding.recyclerViewMessages.scrollToPosition(messageList.size - 1)
                binding.editTextMessage.text.clear()

                // 2. Add temporary "Thinking..." loading message to UI
                val loadingMessage = ChatMessage("Thinking...", isUser = false, isLoading = true)
                chatAdapter.addMessage(loadingMessage)
                binding.recyclerViewMessages.scrollToPosition(messageList.size - 1)

                // 3. Inject prompt into background WebView
                val escapedText = text.replace("'", "\\'")
                val jsCode = currentProvider.promptJs.replace("PROMPT_TEXT", escapedText)
                binding.webViewGemini.evaluateJavascript(jsCode, null)

                // 4. Get initial response elements count in DOM, then poll for new elements
                val getCountJs = "document.querySelectorAll('.model-response-text, message-content, .markdown, model-response p, div[data-message-id]').length;"
                binding.webViewGemini.evaluateJavascript(getCountJs) { countStr ->
                    val initialCount = countStr?.toIntOrNull() ?: 0
                    pollForResponse(attemptsLeft = 35, initialCount = initialCount)
                }
            }
        }
    }

    private fun loadChatHistory() {
        val history = dbHelper.getAllMessages()
        if (history.isNotEmpty()) {
            messageList.clear()
            messageList.addAll(history)
            chatAdapter.notifyDataSetChanged()
            binding.recyclerViewMessages.scrollToPosition(messageList.size - 1)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val webSettings: WebSettings = binding.webViewGemini.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webSettings.userAgentString = UserAgentManager.getRandomUserAgent()

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(binding.webViewGemini, true)

        binding.webViewGemini.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }
    }

    private fun loadProviderUrl(url: String) {
        binding.webViewGemini.loadUrl(url)
    }

    private fun clearChatAndResetSession() {
        // Clear UI and DB
        chatAdapter.clearMessages()
        dbHelper.clearHistory()

        // Clear Web Storage & Cookies
        WebStorage.getInstance().deleteAllData()
        CookieManager.getInstance().removeAllCookies(null)
        binding.webViewGemini.clearCache(true)
        binding.webViewGemini.clearHistory()

        // Assign a new random device User-Agent
        val newUserAgent = UserAgentManager.getRandomUserAgent()
        binding.webViewGemini.settings.userAgentString = newUserAgent

        // Reload current provider URL
        loadProviderUrl(currentProvider.url)

        Toast.makeText(this, "Chat cleared & Session refreshed with new device identity!", Toast.LENGTH_SHORT).show()
    }

    private fun pollForResponse(attemptsLeft: Int, initialCount: Int) {
        val scrapeJs = """
            (function() {
                var elements = document.querySelectorAll('.model-response-text, message-content, .markdown, model-response p, div[data-message-id]');
                if (elements.length > $initialCount) {
                    var el = elements[elements.length - 1];
                    var contentEl = el.querySelector('.markdown') || el.querySelector('.model-response-text') || el;
                    var html = contentEl.innerHTML;
                    if (html && html.trim().length > 0) {
                        return html;
                    }
                }
                return "";
            })();
        """.trimIndent()

        binding.webViewGemini.evaluateJavascript(scrapeJs) { response ->
            val cleaned = response?.removeSurrounding("\"")
                ?.replace("\\\"", "\"")
                ?.replace("\\\\", "\\")
                ?.replace("\\n", "\n")
                ?.replace("\\t", "\t") ?: ""

            if (cleaned.isNotBlank() && cleaned != "null" && cleaned != "\"\"") {
                // New response generated and added to DOM!
                chatAdapter.updateLastMessage(cleaned)
                val aiMessage = ChatMessage(cleaned, isUser = false)
                dbHelper.insertMessage(aiMessage, currentProvider.displayName)
                binding.recyclerViewMessages.scrollToPosition(messageList.size - 1)
            } else if (attemptsLeft > 0) {
                // Poll again in 1 second until the new response element appears
                handler.postDelayed({ pollForResponse(attemptsLeft - 1, initialCount) }, 1000)
            } else {
                chatAdapter.removeLastMessage()
                Toast.makeText(this, "Response timeout on ${currentProvider.displayName}", Toast.LENGTH_LONG).show(
                )
            }
        }
    }
}
