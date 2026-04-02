package com.example.axf_movile.ui.reportes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.axf_movile.R
import com.example.axf_movile.databinding.ItemReporteBinding
import com.example.axf_movile.models.Reporte
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ReportesAdapter(
    private val onClick: (Reporte) -> Unit
) : ListAdapter<Reporte, ReportesAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Reporte>() {
        override fun areItemsTheSame(a: Reporte, b: Reporte) = a.idReporte == b.idReporte
        override fun areContentsTheSame(a: Reporte, b: Reporte) = a == b
    }

    inner class ViewHolder(val binding: ItemReporteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reporte: Reporte) {
            binding.apply {
                // Categoría — formato legible
                val catLabel = categoriaLabel(reporte.categoria)
                tvCategoria.text = catLabel

                // Color del dot según categoría
                val dotColor = categoriaColor(reporte.categoria, root.context)
                dotCategoria.background.setTint(dotColor)

                // Descripción
                tvDescripcion.text = reporte.descripcion

                // Estado badge
                tvEstado.text = estadoLabel(reporte.estado)
                val estadoBg = when (reporte.estado) {
                    "Abierto"    -> R.drawable.bg_estado_abierto
                    "En_Proceso" -> R.drawable.bg_estado_proceso
                    "Resuelto"   -> R.drawable.bg_estado_resuelto
                    else         -> R.drawable.bg_estado_abierto
                }
                tvEstado.setBackgroundResource(estadoBg)
                val estadoColor = when (reporte.estado) {
                    "Abierto"    -> root.context.getColor(R.color.error)
                    "En_Proceso" -> root.context.getColor(R.color.warning)
                    "Resuelto"   -> root.context.getColor(R.color.success)
                    else         -> root.context.getColor(R.color.error)
                }
                tvEstado.setTextColor(estadoColor)

                // Strikes
                if (reporte.numStrikes > 0) {
                    tvStrikes.visibility = View.VISIBLE
                    val strikeColor = when {
                        reporte.numStrikes >= 3 -> root.context.getColor(R.color.error)
                        reporte.numStrikes == 2 -> root.context.getColor(R.color.strike_2)
                        else                    -> root.context.getColor(R.color.warning)
                    }
                    tvStrikes.setTextColor(strikeColor)
                    tvStrikes.text = "⚡ ${reporte.numStrikes}"
                } else {
                    tvStrikes.visibility = View.GONE
                }

                // Foto
                val fotoUrl = reporte.fotoUrl
                if (!fotoUrl.isNullOrEmpty()) {
                    ivFoto.visibility = View.VISIBLE
                    Glide.with(root.context)
                        .load(fotoUrl)
                        .centerCrop()
                        .placeholder(R.drawable.bg_image_placeholder)
                        .into(ivFoto)
                } else {
                    ivFoto.visibility = View.GONE
                }

                // Sucursal
                tvSucursal.text = reporte.nombreSucursal ?: "Sucursal"

                // Sumados
                val sumados = reporte.numSumados ?: 0
                if (sumados > 0) {
                    tvSumados.visibility = View.VISIBLE
                    tvSumados.text = "👥 $sumados"
                } else {
                    tvSumados.visibility = View.GONE
                }

                // Fecha relativa
                tvFecha.text = tiempoRelativo(reporte.creadoEn)

                root.setOnClickListener { onClick(reporte) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReporteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun categoriaLabel(cat: String) = when (cat) {
        "Maquina_Dañada"    -> "🔧 Máquina Dañada"
        "Baño_Tapado"       -> "🚽 Baño Tapado"
        "Problema_Limpieza" -> "🧹 Problema de Limpieza"
        "Reporte_Personal"  -> "👤 Reporte de Personal"
        else                -> "📋 Otro"
    }

    private fun categoriaColor(cat: String, ctx: android.content.Context) = when (cat) {
        "Maquina_Dañada"    -> ctx.getColor(R.color.cat_maquina)
        "Baño_Tapado"       -> ctx.getColor(R.color.cat_banio)
        "Problema_Limpieza" -> ctx.getColor(R.color.cat_limpieza)
        "Reporte_Personal"  -> ctx.getColor(R.color.cat_personal)
        else                -> ctx.getColor(R.color.cat_otro)
    }

    private fun estadoLabel(estado: String) = when (estado) {
        "Abierto"    -> "Abierto"
        "En_Proceso" -> "En Proceso"
        "Resuelto"   -> "Resuelto"
        else         -> estado
    }

    private fun tiempoRelativo(dateStr: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(dateStr)
                ?: SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dateStr)
                ?: return dateStr

            val diff = System.currentTimeMillis() - date.time
            val mins  = TimeUnit.MILLISECONDS.toMinutes(diff)
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            val days  = TimeUnit.MILLISECONDS.toDays(diff)

            when {
                mins  < 1   -> "Ahora mismo"
                mins  < 60  -> "Hace $mins min"
                hours < 24  -> "Hace $hours h"
                days  == 1L -> "Ayer"
                days  < 7   -> "Hace $days días"
                else        -> SimpleDateFormat("dd MMM", Locale("es","MX")).format(date)
            }
        } catch (e: Exception) { dateStr }
    }
}
