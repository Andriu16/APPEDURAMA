package com.example.appedurama.ui.notifications.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appedurama.R
import com.example.appedurama.data.model.RutaAprendizaje
import com.example.appedurama.databinding.ItemLearningPathBinding

class RutaAprendizajeAdapter(
    private val onToggleCursos: (RutaAprendizaje) -> Unit,
    private val onToggleHabilidades: (RutaAprendizaje) -> Unit,
    private val onToggleOportunidades: (RutaAprendizaje) -> Unit,
    private val onItemClicked: (RutaAprendizaje) -> Unit
) : ListAdapter<RutaAprendizaje, RutaAprendizajeAdapter.RutaViewHolder>(RutaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RutaViewHolder {
        val binding = ItemLearningPathBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RutaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RutaViewHolder, position: Int) {
        val ruta = getItem(position)
        holder.bind(ruta, onToggleCursos, onToggleHabilidades, onToggleOportunidades, onItemClicked)
    }

    class RutaViewHolder(private val binding: ItemLearningPathBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            ruta: RutaAprendizaje,
            onToggleCursos: (RutaAprendizaje) -> Unit,
            onToggleHabilidades: (RutaAprendizaje) -> Unit,
            onToggleOportunidades: (RutaAprendizaje) -> Unit,
            onItemClicked: (RutaAprendizaje) -> Unit
        ) {
            binding.tvTitle.text = ruta.titulo
            binding.tvDescription.text = ruta.descripcion

            // Configurar los listeners para los botones de expandir
            binding.btnCursos.setOnClickListener { onToggleCursos(ruta) }
            binding.btnHabilidades.setOnClickListener { onToggleHabilidades(ruta) }
            binding.btnOportunidades.setOnClickListener { onToggleOportunidades(ruta) }
            itemView.setOnClickListener {
                onItemClicked(ruta)
            }

            // Actualizar la UI (visibilidad y flechas) basado en el estado
            updateCursos(ruta)
            updateHabilidades(ruta)
            updateOportunidades(ruta)
        }

        // Funciones para actualizar cada sección
        private fun updateCursos(ruta: RutaAprendizaje) {
            binding.recyclerCursos.isVisible = ruta.cursosExpandido
            binding.ivCursosArrow.setImageResource(
                if (ruta.cursosExpandido) R.drawable.ic_less else R.drawable.ic_expand_more
            )
            if (ruta.cursosExpandido) {
                binding.recyclerCursos.layoutManager = LinearLayoutManager(itemView.context)
                binding.recyclerCursos.adapter = SimpleTextAdapter(ruta.cursos)
            }
        }

        private fun updateHabilidades(ruta: RutaAprendizaje) {
            binding.recyclerHabilidades.isVisible = ruta.habilidadesExpandido
            binding.ivHabilidadesArrow.setImageResource(
                if (ruta.habilidadesExpandido) R.drawable.ic_less else R.drawable.ic_expand_more
            )
            if (ruta.habilidadesExpandido) {
                binding.recyclerHabilidades.layoutManager = LinearLayoutManager(itemView.context)
                binding.recyclerHabilidades.adapter = SimpleTextAdapter(ruta.habilidades)
            }
        }

        private fun updateOportunidades(ruta: RutaAprendizaje) {
            binding.recyclerOportunidades.isVisible = ruta.oportunidadesExpandido
            binding.ivOportunidadesArrow.setImageResource(
                if (ruta.oportunidadesExpandido) R.drawable.ic_less else R.drawable.ic_expand_more
            )
            if (ruta.oportunidadesExpandido) {
                binding.recyclerOportunidades.layoutManager = LinearLayoutManager(itemView.context)
                binding.recyclerOportunidades.adapter = SimpleTextAdapter(ruta.oportunidades)
            }
        }
    }
}

// DiffUtil para que ListAdapter sepa qué items han cambiado, mejorando el rendimiento
class RutaDiffCallback : DiffUtil.ItemCallback<RutaAprendizaje>() {
    override fun areItemsTheSame(oldItem: RutaAprendizaje, newItem: RutaAprendizaje): Boolean {
        return oldItem.titulo == newItem.titulo // Usa un ID único si lo tuvieras
    }

    override fun areContentsTheSame(oldItem: RutaAprendizaje, newItem: RutaAprendizaje): Boolean {
        return oldItem == newItem // Compara todos los campos gracias a la data class
    }
}