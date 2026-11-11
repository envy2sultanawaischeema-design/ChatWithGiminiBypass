package com.example.chatwithgiminibypass.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.chatwithgiminibypass.ChatMessage

class ChatDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "chat_history.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_MESSAGES = "messages"

        private const val KEY_ID = "id"
        private const val KEY_TEXT = "text"
        private const val KEY_IS_USER = "is_user"
        private const val KEY_PROVIDER = "provider"
        private const val KEY_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE " + TABLE_MESSAGES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TEXT + " TEXT,"
                + KEY_IS_USER + " INTEGER,"
                + KEY_PROVIDER + " TEXT,"
                + KEY_TIMESTAMP + " LONG" + ")")
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MESSAGES")
        onCreate(db)
    }

    fun insertMessage(message: ChatMessage, provider: String = "Gemini"): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_TEXT, message.text)
            put(KEY_IS_USER, if (message.isUser) 1 else 0)
            put(KEY_PROVIDER, provider)
            put(KEY_TIMESTAMP, message.timestamp)
        }
        val id = db.insert(TABLE_MESSAGES, null, values)
        db.close()
        return id
    }

    fun getAllMessages(): List<ChatMessage> {
        val messageList = mutableListOf<ChatMessage>()
        val selectQuery = "SELECT * FROM $TABLE_MESSAGES ORDER BY $KEY_ID ASC"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val text = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TEXT))
                val isUser = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_USER)) == 1
                val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TIMESTAMP))
                messageList.add(ChatMessage(text, isUser, timestamp))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return messageList
    }

    fun clearHistory() {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM $TABLE_MESSAGES")
        db.close()
    }
}
