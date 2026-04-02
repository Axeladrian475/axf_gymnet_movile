package com.example.axf_movile.ui.reportes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.axf_movile.R
import com.example.axf_movile.databinding.FragmentReporteDetalleBinding
import com.example.axf_movile.models.Reporte
import com.example.axf_movile.network.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ReporteDetalleFragment : Fragment() {

    private var _binding: FragmentReporteDetalleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportesViewModel by activityViewModels()
    private var idReporte: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReporteDetalleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        idReporte = arguments?.getInt("id_reporte") ?: -1

        setupToolbar()
        setupObservers()

        if (idReporte != -1) viewModel.loadReporteDetalle(idReporte)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObservers() {
        viewModel.reporteDetalle.observe(viewLifecycleOwner) { reporte ->
            reporte?.let { renderReporte(it) }
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ReportesUiState.Loading -> {
                    binding.btnSumarse.isEnabled = false
                }
                is ReportesUiState.Success -> {
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                    viewModel.resetUiState()
                }
                is ReportesUiState.Error -> {
                    binding.btnSumarse.isEnabled = true
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(requireContext().getColor(R.color.error))
                        .show()
                    viewModel.resetUiState()
                }
                else -> { binding.btnSumarse.isEnabled = true }
            }
        }
    }

    private fun renderReporte(reporte: Reporte) {
        val miId = SessionManager.getIdSuscriptor()
        val esMioElReporte = reporte.idSuscriptor == miId

        // ── Categoría y estado ───────────────────────────────────────────────
        binding.tvCategoria.text = categoriaLabel(reporte.categoria)

        val dotColor = categoriaColor(reporte.categoria)
        binding.dotCategoria.background.setTint(dotColor)

        binding.tvEstado.text = estadoLabel(reporte.estado)
        val (estadoBg, estadoColor) = when (reporte.estado) {
            "Abierto"    -> R.drawable.bg_estado_abierto to R.color.error
            "En_Proceso" -> R.drawable.bg_estado_proceso to R.color.warning
            "Resuelto"   -> R.drawable.bg_estado_resuelto to R.color.success
            else         -> R.drawable.bg_estado_abierto to R.color.error
        }
        binding.tvEstado.setBackgroundResource(estadoBg)
        binding.tvEstado.setTextColor(requireContext().getColor(estadoColor))

        // ── Descripción ──────────────────────────────────────────────────────
        binding.tvDescripcion.text = reporte.descripcion

        // ── Foto ────────────────────────────────────────────────────────────
        if (!reporte.fotoUrl.isNullOrEmpty()) {
            binding.ivFoto.visibility = View.VISIBLE
            Glide.with(this)
                .load(reporte.fotoUrl)
                .centerCrop()
                .placeholder(R.drawable.bg_image_placeholder)
                .into(binding.ivFoto)
        } else {
            binding.ivFoto.visibility = View.GONE
        }

        // ── Info ────────────────────────────────────────────────────────────
        binding.tvSucursal.text     = reporte.nombreSucursal ?: "—"
        binding.tvReportadoPor.text = reporte.nombreSuscriptor ?: "Anónimo"
        binding.tvFecha.text        = formatFechaCompleta(reporte.creadoEn)

        // ── Sistema de Strikes ───────────────────────────────────────────────
        val strikes = reporte.numStrikes
        if (strikes > 0 && reporte.estado != "Resuelto") {
            binding.bannerStrikes.visibility = View.VISIBLE

            // Color e ícono según nivel
            val (bannerBg, strikeLabelColor, strikeTexto) = when {
                strikes >= 3 -> Triple(
                    R.drawable.bg_strike_3,
                    requireContext().getColor(R.color.error),
                    "Tu reporte lleva 72 horas sin resolución. El gerente ha sido notificado. " +
                        "Puedes reenviar el reporte directamente al gerente."
                )
                strikes == 2 -> Triple(
                    R.drawable.bg_strike_1,
                    requireContext().getColor(R.color.strike_2),
                    "Tu reporte lleva 48 horas sin atención. El personal y el gerente han sido alertados."
                )
                else -> Triple(
                    R.drawable.bg_strike_1,
                    requireContext().getColor(R.color.warning),
                    "Tu reporte lleva 24 horas sin seguimiento. El personal de la sucursal ha sido notificado."
                )
            }

            binding.bannerStrikes.setBackgroundResource(bannerBg)
            binding.tvStrikesBadge.text = "Strike $strikes"
            binding.tvStrikesBadge.setTextColor(strikeLabelColor)
            binding.tvStrikesDesc.text  = strikeTexto

            // Botón reenviar al gerente (solo si strike 3 y es reporte propio)
            if (strikes >= 3 && esMioElReporte) {
                binding.btnReenviarGerente.visibility = View.VISIBLE
                binding.btnReenviarGerente.setOnClickListener {
                    confirmarReenvioGerente(reporte.idReporte)
                }
            } else {
                binding.btnReenviarGerente.visibility = View.GONE
            }
        } else {
            binding.bannerStrikes.visibility = View.GONE
        }

        // ── Botón sumarse ────────────────────────────────────────────────────
        // Solo visible si: reporte público, no es mío, estado Abierto/En_Proceso
        val puedeVerSumarse = !esMioElReporte &&
            reporte.esPrivado == 0 &&
            reporte.estado != "Resuelto"

        if (puedeVerSumarse) {
            if (reporte.yaSumado == true) {
                binding.btnSumarse.visibility  = View.GONE
                binding.tvYaSumado.visibility  = View.VISIBLE
            } else {
                binding.btnSumarse.visibility  = View.VISIBLE
                binding.tvYaSumado.visibility  = View.GONE
                binding.btnSumarse.setOnClickListener {
                    confirmarSumarse(reporte.idReporte)
                }
            }
        } else {
            binding.btnSumarse.visibility  = View.GONE
            binding.tvYaSumado.visibility  = View.GONE
        }

        // ── Contador sumados ─────────────────────────────────────────────────
        val sumados = reporte.numSumados ?: 0
        if (sumados > 0) {
            binding.tvSumados.visibility = View.VISIBLE
            binding.tvSumados.text = "👥 $sumados persona${if (sumados == 1) "" else "s"} " +
                "${if (sumados == 1) "se ha sumado" else "se han sumado"} a este reporte"
        } else {
            binding.tvSumados.visibility = View.GONE
        }
    }

    // ─── Diálogos ─────────────────────────────────────────────────────────────

    private fun confirmarSumarse(idReporte: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sumarse al reporte")
            .setMessage("¿Confirmas que tienes el mismo problema?\n\nRecibirás notificaciones cuando haya actualizaciones.")
            .setPositiveButton("Sí, sumarme") { _, _ ->
                viewModel.sumarseReporte(idReporte)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarReenvioGerente(idReporte: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("🔴 Reenviar al Gerente")
            .setMessage("Este reporte será escalado directamente al gerente de la sucursal con prioridad máxima.\n\n¿Deseas continuar?")
            .setPositiveButton("Reenviar") { _, _ ->
                // En producción: llamar endpoint de reenvío
                Snackbar.make(
                    binding.root,
                    "Reporte enviado directamente al gerente",
                    Snackbar.LENGTH_LONG
                ).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun categoriaLabel(cat: String) = when (cat) {
        "Maquina_Dañada"    -> "🔧 Máquina Dañada"
        "Baño_Tapado"       -> "🚽 Baño Tapado"
        "Problema_Limpieza" -> "🧹 Problema de Limpieza"
        "Reporte_Personal"  -> "👤 Reporte de Personal"
        else                -> "📋 Otro"
    }

    private fun categoriaColor(cat: String) = when (cat) {
        "Maquina_Dañada"    -> requireContext().getColor(R.color.cat_maquina)
        "Baño_Tapado"       -> requireContext().getColor(R.color.cat_banio)
        "Problema_Limpieza" -> requireContext().getColor(R.color.cat_limpieza)
        "Reporte_Personal"  -> requireContext().getColor(R.color.cat_personal)
        else                -> requireContext().getColor(R.color.cat_otro)
    }

    private fun estadoLabel(estado: String) = when (estado) {
        "Abierto"    -> "Abierto"
        "En_Proceso" -> "En Proceso"
        "Resuelto"   -> "✅ Resuelto"
        else         -> estado
    }

    private fun formatFechaCompleta(dateStr: String): String {
        return try {
            val parsers = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm:ss"
            )
            var date: java.util.Date? = null
            for (pattern in parsers) {
                try {
                    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                    date = sdf.parse(dateStr)
                    if (date != null) break
                } catch (_: Exception) {}
            }
            date?.let {
                SimpleDateFormat("dd 'de' MMMM yyyy, HH:mm", Locale("es", "MX")).format(it)
            } ?: dateStr
        } catch (e: Exception) { dateStr }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
