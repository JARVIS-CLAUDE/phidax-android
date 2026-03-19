package org.phidax

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var inputField: EditText
    private lateinit var sendButton: FloatingActionButton
    private lateinit var statusText: TextView

    private lateinit var adapter: MessageAdapter
    private val messages = mutableListOf<Message>()

    private lateinit var wsManager: WebSocketManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        inputField = findViewById(R.id.inputField)
        sendButton = findViewById(R.id.sendButton)
        statusText = findViewById(R.id.statusText)

        setupRecyclerView()
        setupWebSocket()
        setupSendButton()
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter(this, messages)
        recyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager
    }

    private fun setupWebSocket() {
        wsManager = WebSocketManager(
            onMessageReceived = { text ->
                runOnUiThread {
                    addMessage(text, isUser = false)
                }
            },
            onStatusChanged = { connected ->
                runOnUiThread {
                    statusText.text = if (connected) "Connecté" else "Déconnecté"
                }
            }
        )
        wsManager.connect()
    }

    private fun setupSendButton() {
        sendButton.setOnClickListener {
            val text = inputField.text.toString().trim()
            if (text.isNotEmpty()) {
                addMessage(text, isUser = true)
                wsManager.sendMessage(text)
                inputField.text.clear()
            }
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        val message = Message(text, isUser)
        adapter.addMessage(message)
        recyclerView.scrollToPosition(messages.size - 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        wsManager.disconnect()
    }
}
