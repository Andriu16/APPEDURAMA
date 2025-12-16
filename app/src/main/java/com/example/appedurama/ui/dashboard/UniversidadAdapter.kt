package com.example.appedurama.ui.dashboard

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appedurama.data.model.UniversidadRecomendada
import com.example.appedurama.databinding.ItemUniversidadBinding

class UniversidadAdapter : ListAdapter<UniversidadRecomendada, UniversidadAdapter.UniversidadViewHolder>(UniversidadDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UniversidadViewHolder {
        val binding = ItemUniversidadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UniversidadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UniversidadViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UniversidadViewHolder(private val binding: ItemUniversidadBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(universidad: UniversidadRecomendada) {
            binding.tvNombreUniversidad.text = universidad.nombre
            binding.tvDescripcionRecomendacion.text = universidad.descripcion

            binding.btnVisitarSitio.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(universidad.sitioWeb))
                itemView.context.startActivity(intent)
            }
        }
    }
}

class UniversidadDiffCallback : DiffUtil.ItemCallback<UniversidadRecomendada>() {
    override fun areItemsTheSame(oldItem: UniversidadRecomendada, newItem: UniversidadRecomendada): Boolean {
        return oldItem.nombre == newItem.nombre
    }

    override fun areContentsTheSame(oldItem: UniversidadRecomendada, newItem: UniversidadRecomendada): Boolean {
        return oldItem == newItem
    }
}