package com.example.appedurama.ui.notifications.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appedurama.data.model.OportunidadProfesional
import com.example.appedurama.databinding.ItemOportunidadBinding

class OportunidadAdapter :
    ListAdapter<OportunidadProfesional, OportunidadAdapter.OportunidadViewHolder>(OportunidadDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OportunidadViewHolder {
        val binding = ItemOportunidadBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OportunidadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OportunidadViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class OportunidadViewHolder(private val binding: ItemOportunidadBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(oportunidad: OportunidadProfesional) {
            binding.textViewPuesto.text = "• ${oportunidad.puesto}"
            binding.textViewSalario.text = oportunidad.salarioEstimado
        }
    }
}

class OportunidadDiffCallback : DiffUtil.ItemCallback<OportunidadProfesional>() {
    override fun areItemsTheSame(oldItem: OportunidadProfesional, newItem: OportunidadProfesional): Boolean {
        return oldItem.puesto == newItem.puesto
    }

    override fun areContentsTheSame(oldItem: OportunidadProfesional, newItem: OportunidadProfesional): Boolean {
        return oldItem == newItem
    }
}