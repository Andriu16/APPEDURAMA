package com.example.appedurama.ui.cursos_inscritos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.appedurama.R
import com.example.appedurama.data.model.CursoInscrito
import com.example.appedurama.databinding.ItemCursoAfiliadoBinding

class CursosInscritosAdapter : ListAdapter<CursoInscrito, CursosInscritosAdapter.CursoInscritoViewHolder>(CursoInscritoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CursoInscritoViewHolder {
        val binding = ItemCursoAfiliadoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CursoInscritoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CursoInscritoViewHolder, position: Int) {
        val curso = getItem(position)
        holder.bind(curso, position + 1)
    }

    class CursoInscritoViewHolder(private val binding: ItemCursoAfiliadoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(curso: CursoInscrito, position: Int) {
            binding.tvCourseNumber.text = String.format("%02d", position)
            binding.tvCourseTitle.text = curso.titulo
            binding.tvCourseDescription.text = curso.descripcion ?: "Sin descripción disponible."

            binding.ivBackground.load(curso.imagenUrl) {
                crossfade(true)
                placeholder(R.drawable.imgecel)
                error(R.drawable.imgecel)
            }

            val logoResId = when (curso.plataforma?.lowercase()) {
                "udemy" -> R.drawable.ic_udemy_d
                "platzi" -> R.drawable.ic_platzi
                "upc" -> R.drawable.ic_upc
                "ulima" -> R.drawable.ic_ulima
                else -> R.drawable.ic_book
            }
            binding.ivCourseLogo.setImageResource(logoResId)
        }
    }
}

class CursoInscritoDiffCallback : DiffUtil.ItemCallback<CursoInscrito>() {
    override fun areItemsTheSame(oldItem: CursoInscrito, newItem: CursoInscrito): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CursoInscrito, newItem: CursoInscrito): Boolean {
        return oldItem == newItem
    }
}