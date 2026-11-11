package com.example.chatwithgiminibypass

enum class AiProvider(
    val displayName: String,
    val url: String,
    val promptJs: String,
    val scrapeJs: String
) {
    GEMINI(
        displayName = "Gemini AI",
        url = "https://gemini.google.com/app",
        promptJs = """
            (function() {
                var el = document.querySelector('rich-textarea p') || document.querySelector('div[contenteditable="true"]');
                if (el) {
                    el.focus();
                    el.innerText = 'PROMPT_TEXT';
                    el.dispatchEvent(new Event('input', { bubbles: true }));
                    setTimeout(function() {
                        var sendBtn = document.querySelector('button.send-button') || document.querySelector('button[aria-label*="Send"]');
                        if (sendBtn) {
                            sendBtn.removeAttribute('disabled');
                            sendBtn.click();
                        }
                    }, 500);
                }
            })();
        """.trimIndent(),
        scrapeJs = """
            (function() {
                var elements = document.querySelectorAll('.model-response-text, message-content, .markdown, model-response p');
                if (elements.length > 0) {
                    var text = elements[elements.length - 1].innerText;
                    if (text && text.trim().length > 0) {
                        return text;
                    }
                }
                return "";
            })();
        """.trimIndent()
    ),

    CHAT_GPT(
        displayName = "ChatGPT",
        url = "https://chatgpt.com/",
        promptJs = """
            (function() {
                var el = document.querySelector('#prompt-textarea') || document.querySelector('div[contenteditable="true"]') || document.querySelector('textarea');
                if (el) {
                    el.focus();
                    if (el.tagName === 'TEXTAREA' || el.tagName === 'INPUT') {
                        el.value = 'PROMPT_TEXT';
                    } else {
                        el.innerHTML = '<p>PROMPT_TEXT</p>';
                    }
                    el.dispatchEvent(new InputEvent('input', { bubbles: true, inputType: 'insertText', data: 'PROMPT_TEXT' }));
                    el.dispatchEvent(new Event('change', { bubbles: true }));
                    setTimeout(function() {
                        var btn = document.querySelector('button[data-testid="send-button"]') || document.querySelector('button[aria-label*="Send"]');
                        if (btn) {
                            btn.removeAttribute('disabled');
                            btn.click();
                        }
                    }, 600);
                }
            })();
        """.trimIndent(),
        scrapeJs = """
            (function() {
                var elements = document.querySelectorAll('.markdown, [data-message-author-role="assistant"], .agent-turn');
                if (elements.length > 0) {
                    var text = elements[elements.length - 1].innerText;
                    if (text && text.trim().length > 0) {
                        return text;
                    }
                }
                return "";
            })();
        """.trimIndent()
    ),

    DEEPSEEK(
        displayName = "DeepSeek AI",
        url = "https://chat.deepseek.com/",
        promptJs = """
            (function() {
                var el = document.querySelector('textarea') || document.querySelector('div[contenteditable="true"]');
                if (el) {
                    el.focus();
                    if (el.tagName === 'TEXTAREA') {
                        el.value = 'PROMPT_TEXT';
                    } else {
                        el.innerText = 'PROMPT_TEXT';
                    }
                    el.dispatchEvent(new InputEvent('input', { bubbles: true, inputType: 'insertText', data: 'PROMPT_TEXT' }));
                    setTimeout(function() {
                        var btn = document.querySelector('button[type="submit"]') || document.querySelector('.ds-icon-button') || document.querySelector('div.f9573887');
                        if (btn) {
                            btn.removeAttribute('disabled');
                            btn.click();
                        }
                    }, 600);
                }
            })();
        """.trimIndent(),
        scrapeJs = """
            (function() {
                var elements = document.querySelectorAll('.ds-markdown, .markdown, .assistant-response, div[class*="markdown"]');
                if (elements.length > 0) {
                    var text = elements[elements.length - 1].innerText;
                    if (text && text.trim().length > 0) {
                        return text;
                    }
                }
                return "";
            })();
        """.trimIndent()
    ),

    DUCKDUCKGO_AI(
        displayName = "DuckDuckGo Free AI",
        url = "https://duckduckgo.com/chat",
        promptJs = """
            (function() {
                var el = document.querySelector('textarea') || document.querySelector('div[contenteditable="true"]');
                if (el) {
                    el.focus();
                    if (el.tagName === 'TEXTAREA') {
                        el.value = 'PROMPT_TEXT';
                    } else {
                        el.innerText = 'PROMPT_TEXT';
                    }
                    el.dispatchEvent(new InputEvent('input', { bubbles: true, inputType: 'insertText', data: 'PROMPT_TEXT' }));
                    setTimeout(function() {
                        var btn = document.querySelector('button[type="submit"]') || document.querySelector('button[aria-label*="Send"]');
                        if (btn) {
                            btn.removeAttribute('disabled');
                            btn.click();
                        }
                    }, 600);
                }
            })();
        """.trimIndent(),
        scrapeJs = """
            (function() {
                var elements = document.querySelectorAll('[data-testid="message-text"], .markdown, .chat-line');
                if (elements.length > 0) {
                    var text = elements[elements.length - 1].innerText;
                    if (text && text.trim().length > 0) {
                        return text;
                    }
                }
                return "";
            })();
        """.trimIndent()
    )
}
