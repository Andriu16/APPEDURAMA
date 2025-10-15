package com.example.appedurama.ui.notifications.cursos.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.appedurama.R
import com.example.appedurama.data.model.Curso
import com.example.appedurama.databinding.CardviewCursosBinding
import com.example.appedurama.ui.notifications.cursos.CursoDetalleDialogFragment

class CursosAdapter(private val fragmentManager: androidx.fragment.app.FragmentManager) :
    ListAdapter<Curso, CursosAdapter.CursoViewHolder>(CursoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CursoViewHolder {
        val binding = CardviewCursosBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CursoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CursoViewHolder, position: Int) {
        val curso = getItem(position)
        holder.bind(curso, position + 1)
    }

    inner class CursoViewHolder(private val binding: CardviewCursosBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(curso: Curso, position: Int) {
            // --- 1. DATOS COMUNES (SIN CAMBIOS) ---
            binding.tvCourseTitle.text = curso.nombre
            binding.tvCourseNumber.text = String.format("%02d", position)

            binding.ivBackground.load(curso.imagen) {
                crossfade(true)
                placeholder(R.drawable.imgecel)
                error(R.drawable.imgecel)
            }
            val logoResId = when (curso.plataforma.lowercase()) {
                "udemy" -> R.drawable.ic_udemy_d
                // Asegúrate de tener estos drawables en tu proyecto
                "platzi" -> R.drawable.ic_platzi
                "upc" -> R.drawable.ic_upc
                "ulima" -> R.drawable.ic_ulima
                else -> R.drawable.ic_book
            }
            binding.ivCourseLogo.setImageResource(logoResId)

            binding.ivViewDetails.setOnClickListener {
                // Creamos una instancia de nuestro diálogo pasándole el ID del curso
                val dialog = CursoDetalleDialogFragment.newInstance(curso.id)
                // Mostramos el diálogo usando el FragmentManager
                dialog.show(fragmentManager, "CursoDetalleDialog")
            }

            // --- 2. LÓGICA INTELIGENTE PARA DATOS VARIABLES ---
            // Usamos un 'when' para decidir qué datos y emojis mostrar
            when (curso.plataforma.lowercase()) {
                "udemy" -> {
                    binding.emoji1.text = "⭐" // Emoji de estrella para rating
                    binding.tvRating.text = curso.rating ?: "N/A"

                    binding.emoji2.text = "👥" // Emoji de personas para lectores
                    binding.tvUsers.text = curso.lectores ?: "N/A"
                }
                "platzi" -> {
                    binding.emoji1.text = "🕒" // Emoji de reloj para duración
                    // Limpiamos el texto para que sea más corto
                    binding.tvRating.text = curso.horasContenido?.replace(" de contenido", "") ?: "N/A"

                    binding.emoji2.text = "📶" // Emoji de señal para dificultad
                    binding.tvUsers.text = curso.dificultad?.replace("Nivel ", "") ?: "N/A"
                }
                "upc" -> {
                    binding.emoji1.text = "💵" // Emoji de dinero para precio
                    binding.tvRating.text = "S/ ${curso.precioDescuento ?: "N/A"}"

                    binding.emoji2.text = "📅" // Emoji de calendario para fecha
                    binding.tvUsers.text = curso.fechaInicio ?: "N/A"
                }
                "ulima" -> {
                    binding.emoji1.text = "💵" // Emoji de dinero para precio
                    binding.tvRating.text = curso.precio ?: "N/A"

                    binding.emoji2.text = "💻" // Emoji de laptop para modalidad
                    binding.tvUsers.text = curso.modalidad ?: "N/A"
                }
                else -> {
                    // Caso por defecto si la plataforma no coincide
                    binding.emoji1.text = "ⓘ"
                    binding.tvRating.text = "Info no disp."
                    binding.emoji2.text = "ⓘ"
                    binding.tvUsers.text = "Info no disp."
                }
            }
        }
    }
}

class CursoDiffCallback : DiffUtil.ItemCallback<Curso>() {
    override fun areItemsTheSame(oldItem: Curso, newItem: Curso): Boolean {
        return oldItem.url == newItem.url
    }

    override fun areContentsTheSame(oldItem: Curso, newItem: Curso): Boolean {
        return oldItem == newItem
    }
}