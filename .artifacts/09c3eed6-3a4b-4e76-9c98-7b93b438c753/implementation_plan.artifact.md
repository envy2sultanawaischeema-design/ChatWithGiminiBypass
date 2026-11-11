# Implementation Plan - Multi-AI Support, Room DB, Device Randomization & Rich Actions

We will enhance `ChatWithGiminiBypass` with multi-provider AI support (Gemini, ChatGPT, DeepSeek, DuckDuckGo Free AI), Room DB persistence, User-Agent device spoofing on Clear Chat, loading indicators, and Copy/Share options.

## User Review Required

> [!IMPORTANT]
> **Key Features & Architecture:**
> 1. **Multi-AI Provider Selector:** Switch between **Gemini**, **ChatGPT**, **DeepSeek**, and **DuckDuckGo Chat** (free no-signup AI).
> 2. **Loading / Preparation State:** Shows a "Thinking..." indicator in the chat until the response is ready, ensuring no incomplete text is displayed prematurely.
> 3. **Clear Chat + Random Device Identity:** "Clear Chat" button clears the screen, resets history, wipes WebView cookies/cache, and assigns a randomized device User-Agent (Android / iOS / Windows / macOS / Linux) for a fresh session.
> 4. **Room Database Persistence:** Automatically saves all chat messages locally in SQLite/Room DB so history persists across app restarts.
> 5. **Copy & Share Options:** Long-clicking any chat bubble opens a context menu with **Copy to Clipboard** and **Share via App** options.
> 6. **Customizable Provider Options:** Dialog/Settings to select/custom-configure AI provider URLs and parameters.

## Proposed Changes

### Database & Persistence
- Add Room Database dependencies (`androidx.room:room-runtime`, `androidx.room:room-compiler`, `androidx.room:room-ktx`) in `app/build.gradle.kts`.
- **[NEW] [ChatEntity.kt](file:///C:/Users/Awais/AndroidStudioProjects/ChatWithGiminiBypass/app/src/main/java/com/example/chatwithgiminibypass/db/ChatEntity.kt)**: Room entity for chat messages.
- **[NEW] [ChatDao.kt](file:///C:/Users/Awais/AndroidStudioProjects/ChatWithGiminiBypass/app/src/main/java/com/example/chatwithgiminibypass/db/ChatDao.kt)**: Data access object for insert, query, and clear.
- **[NEW] [AppDatabase.kt](file:///C:/Users/Awais/AndroidStudioProjects/ChatWithGiminiBypass/app/src/main/java/com/example/chatwithgiminibypass/db/AppDatabase.kt)**: Room database instance.

### AI Provider & Device Spoofing Manager
- **[NEW] [AiProvider.kt](file:///C:/Users/Awais/AndroidStudioProjects/ChatWithGiminiBypass/app/src/main/java/com/example/chatwithgiminibypass/AiProvider.kt)**: Enum & manager for Gemini (`gemini.google.com`), ChatGPT (`chatgpt.com`), DeepSeek (`chat.deepseek.com`), and DuckDuckGo Chat (`duckduckgo.com/chat`). Includes JS injection and response scraping selectors for each provider.
- **[NEW] [UserAgentManager.kt](file:///C:/Users/Awais/AndroidStudioProjects/ChatWithGiminiBypass/app/src/main/java/com/example/chatwithgiminibypass/UserAgentManager.kt)**: Utility generating random realistic device User-Agent strings (Mobile & Desktop).

### UI & Layouts
- **[MODIFY] [activity_main.xml](file:///C:/Users/Awais/AndroidStudioProjects/ChatWithGiminiBypass/app/src/main/res/layout/activity_main.xml)**:
  - Top Toolbar with AI Provider Spinner/Dropdown, "Clear Chat" button, and Settings icon.
- **[MODIFY] [item_chat_message.xml](file:///C:/Users/Awais/AndroidStudioProjects/ChatWithGiminiBypass/app/src/main/res/layout/item_chat_message.xml)**:
  - Add loading animation / indicator for "Thinking..." state.
- **[MODIFY] [ChatAdapter.kt](file:///C:/Users/Awais/AndroidStudioProjects/ChatWithGiminiBypass/app/src/main/java/com/example/chatwithgiminibypass/ChatAdapter.kt)**:
  - Long-click listener on chat bubbles for Copy and Share dialog options.
  - Support for "Thinking..." loading item state.

### Activity Logic
- **[MODIFY] [MainActivity.kt](file:///C:/Users/Awais/AndroidStudioProjects/ChatWithGiminiBypass/app/src/main/java/com/example/chatwithgiminibypass/MainActivity.kt)**:
  - Integrate Room DB to load/save chat history.
  - Dynamic WebView switching & User-Agent rotation on provider change or Clear Chat.
  - Response preparation handler: shows "Thinking..." state until final response is scraped, then updates item in database and RecyclerView.

## Verification Plan

### Automated Tests
- Run Gradle build (`app:assembleRelease`) to ensure database schemas and provider modules compile with 0 errors.

### Manual Verification
- Deploy app to device/emulator.
- Test switching providers (Gemini, ChatGPT, DeepSeek, DuckDuckGo Chat).
- Verify "Thinking..." indicator appears while waiting for response.
- Verify "Clear Chat" wipes messages, clears cookies, and assigns a new random User-Agent.
- Verify message long-press shows Copy & Share dialog.
- Verify chat history persists after app restart.
