package com.example.axf_movile.ui.home

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.axf_movile.AxfApplication
import com.example.axf_movile.R
import com.example.axf_movile.databinding.FragmentHomeBinding
import com.example.axf_movile.network.SessionManager
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
        viewModel.loadData()
    }

    private fun setupUI() {
        val nombre = SessionManager.getNombre().split(" ").firstOrNull() ?: "Usuario"
        binding.tvBienvenida.text = "¡Hola, $nombre!"
        binding.tvPuntos.text = "${SessionManager.getPuntos()}"
        binding.tvRacha.text = "${SessionManager.getRachaDias()} días"

        // Botón de logout
        binding.btnLogout.setOnClickListener {
            (activity as? HomeActivity)?.logout()
        }
    }

    private fun setupObservers() {
        viewModel.suscripcion.observe(viewLifecycleOwner) { sus ->
            if (sus != null) {
                binding.shimmerSuscripcion.stopShimmer()
                binding.shimmerSuscripcion.visibility = View.GONE
                binding.cardSuscripcion.visibility = View.VISIBLE

                binding.tvTipoSuscripcion.text = sus.nombreTipo
                binding.tvEstadoSuscripcion.text = sus.estado
                binding.tvFechaVencimiento.text = "Vence: ${formatFecha(sus.fechaFin)}"
                binding.tvSesionesEntrenador.text = "${sus.sesionesEntrenador}"
                binding.tvSesionesNutriologo.text = "${sus.sesionesNutriologo}"

                // Color del estado
                val colorRes = if (sus.estado == "Activa") R.color.success else R.color.error
                binding.tvEstadoSuscripcion.setTextColor(requireContext().getColor(colorRes))
                binding.indicatorEstado.setBackgroundColor(requireContext().getColor(colorRes))

                // Verificar si vence en ≤7 días
                checkRenovacion(sus.fechaFin)

                // Animación entrada
                val anim = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
                binding.cardSuscripcion.startAnimation(anim)

            } else {
                binding.shimmerSuscripcion.stopShimmer()
                binding.shimmerSuscripcion.visibility = View.GONE
                binding.cardSinSuscripcion.visibility = View.VISIBLE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading) {
                binding.shimmerSuscripcion.startShimmer()
                binding.shimmerSuscripcion.visibility = View.VISIBLE
                binding.cardSuscripcion.visibility = View.GONE
                binding.cardSinSuscripcion.visibility = View.GONE
            }
        }
    }

    private fun formatFecha(dateStr: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateStr) ?: return dateStr
            val out = SimpleDateFormat("dd MMM yyyy", Locale("es", "MX"))
            out.format(date)
        } catch (e: Exception) { dateStr }
    }

    private fun checkRenovacion(fechaFin: String) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fin = sdf.parse(fechaFin) ?: return
            val hoy = Date()
            val diff = TimeUnit.MILLISECONDS.toDays(fin.time - hoy.time)

            if (diff in 0..7) {
                sendRenovacionNotification(diff)
            }
        } catch (e: Exception) { /* ignorar */ }
    }

    private fun sendRenovacionNotification(dias: Long) {
        val msg = if (dias == 0L) "¡Tu suscripción vence hoy!"
                  else "Tu suscripción vence en $dias día${if (dias == 1L) "" else "s"}"

        val notification = NotificationCompat.Builder(requireContext(), AxfApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Renueva tu membresía")
            .setContentText(msg)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1001, notification)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
