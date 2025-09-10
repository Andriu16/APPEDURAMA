package com.example.appedurama.ui.notifications.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appedurama.R

class SimpleTextAdapter(private val items: List<String>) :
    RecyclerView.Adapter<SimpleTextAdapter.TextViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_simple_text, parent, false)
        return TextViewHolder(view)
    }

    override fun onBindViewHolder(holder: TextViewHolder, position: Int) {
        // Usamos un bullet point (•) para darle estilo de lista
        holder.textView.text = "• ${items[position]}"
    }

    override fun getItemCount(): Int = items.size

    class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textViewSimple)
    }
}