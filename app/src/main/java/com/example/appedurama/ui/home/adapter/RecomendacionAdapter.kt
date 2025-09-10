package com.example.appedurama.ui.home.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.appedurama.R
import com.example.appedurama.data.model.RecomendacionItem



class RecomendacionAdapter(private val recomendaciones: List<RecomendacionItem>) :
    RecyclerView.Adapter<RecomendacionAdapter.RecomendacionViewHolder>() {

    private val colores = listOf(
        R.color.recomendacion_color_1,
        R.color.recomendacion_color_2,
        R.color.recomendacion_color_3,
        R.color.recomendacion_color_4,
        R.color.recomendacion_color_5
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecomendacionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recomendacion, parent, false)
        return RecomendacionViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecomendacionViewHolder, position: Int) {
        val recomendacion = recomendaciones[position]

        val colorResId = colores[position % colores.size]
        holder.bind(recomendacion,colorResId)
    }

    override fun getItemCount(): Int = recomendaciones.size

    class RecomendacionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView as CardView
        private val titulo: TextView = itemView.findViewById(R.id.textViewTituloRecomendacion)
        private val descripcion: TextView = itemView.findViewById(R.id.textViewDescripcionRecomendacion)

        fun bind(item: RecomendacionItem, colorResId: Int) {
            titulo.text = item.titulo
            descripcion.text = item.descripcion


            val color = ContextCompat.getColor(itemView.context, colorResId)
            cardView.setCardBackgroundColor(color)
        }
    }
}