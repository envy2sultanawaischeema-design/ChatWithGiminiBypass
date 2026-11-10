package com.example.chatwithgiminibypass

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatwithgiminibypass.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessage>()
    private val handler = Handler(Looper.getMainLooper())

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        chatAdapter = ChatAdapter(messageList)
        binding.recyclerViewMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        binding.recyclerViewMessages.adapter = chatAdapter

        // Configure background WebView for Gemini web app
        val webSettings: WebSettings = binding.webViewGemini.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(binding.webViewGemini, true)

        binding.webViewGemini.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }

        binding.webViewGemini.loadUrl("https://gemini.google.com/app")

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

        binding.buttonSend.setOnClickListener {
            val text = binding.editTextMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                // Add user message to RecyclerView
                val userMessage = ChatMessage(text, isUser = true)
                chatAdapter.addMessage(userMessage)
                binding.recyclerViewMessages.scrollToPosition(messageList.size - 1)
                binding.editTextMessage.text.clear()

                // Inject prompt into background WebView
                val escapedText = text.replace("'", "\\'")
                val jsCode = """
                    (function() {
                        var el = document.querySelector('rich-textarea p') || document.querySelector('div[contenteditable="true"]');
                        if (el) {
                            el.innerText = '$escapedText';
                            el.dispatchEvent(new Event('input', { bubbles: true }));
                            setTimeout(function() {
                                var sendBtn = document.querySelector('button.send-button') || document.querySelector('button[aria-label*="Send"]');
                                if (sendBtn) sendBtn.click();
                            }, 500);
                        }
                    })();
                """.trimIndent()

                binding.webViewGemini.evaluateJavascript(jsCode, null)

                // Polling function to robustly extract Gemini response from DOM as it streams/renders
                fun pollForResponse(attemptsLeft: Int) {
                    val scrapeJs = """
                        (function() {
                            var elements = document.querySelectorAll('.model-response-text, message-content, .markdown, model-response p, div[data-message-id]');
                            if (elements.length > 0) {
                                var text = elements[elements.length - 1].innerText;
                                if (text && text.trim().length > 0) {
                                    return text;
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
                            val geminiMessage = ChatMessage(cleaned, isUser = false)
                            chatAdapter.addMessage(geminiMessage)
                            binding.recyclerViewMessages.scrollToPosition(messageList.size - 1)
                        } else if (attemptsLeft > 0) {
                            handler.postDelayed({ pollForResponse(attemptsLeft - 1) }, 1000)
                        } else {
                            val fallbackMessage = ChatMessage("Response took too long or login required in background. Please check connection.", isUser = false)
                            chatAdapter.addMessage(fallbackMessage)
                            binding.recyclerViewMessages.scrollToPosition(messageList.size - 1)
                        }
                    }
                }

                // Start polling after 2 seconds delay to give Gemini time to start generating
                handler.postDelayed({ pollForResponse(8) }, 2000)
            }
        }
    }
}
