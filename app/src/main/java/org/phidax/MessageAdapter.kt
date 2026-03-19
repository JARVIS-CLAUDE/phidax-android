package org.phidax

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(
    private val context: Context,
    private val messages: MutableList<Message>
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userContainer: View = view.findViewById(R.id.userContainer)
        val userText: TextView = view.findViewById(R.id.userText)
        val userCopyButton: ImageView = view.findViewById(R.id.userCopyButton)

        val assistantContainer: View = view.findViewById(R.id.assistantContainer)
        val assistantText: TextView = view.findViewById(R.id.assistantText)
        val assistantCopyButton: ImageView = view.findViewById(R.id.assistantCopyButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        if (message.isUser) {
            holder.userContainer.visibility = View.VISIBLE
            holder.assistantContainer.visibility = View.GONE
            holder.userText.text = message.text
            holder.userCopyButton.setOnClickListener { copyToClipboard(message.text, it) }
        } else {
            holder.userContainer.visibility = View.GONE
            holder.assistantContainer.visibility = View.VISIBLE
            holder.assistantText.text = message.text
            holder.assistantCopyButton.setOnClickListener { copyToClipboard(message.text, it) }
        }
    }

    override fun getItemCount() = messages.size

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    private fun copyToClipboard(text: String, view: View) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("message", text)
        clipboard.setPrimaryClip(clip)

        // Visual feedback
        val originalDrawable = (view as ImageView).drawable
        view.setImageDrawable(
            ContextCompat.getDrawable(context, android.R.drawable.ic_menu_upload)
        )
        view.setColorFilter(ContextCompat.getColor(context, R.color.success_green))

        view.postDelayed({
            view.setImageDrawable(originalDrawable)
            view.clearColorFilter()
        }, 1000)

        Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
    }
}
