package com.example.axf_movile.ui.reportes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.axf_movile.R
import com.example.axf_movile.databinding.FragmentReportesBinding
import com.example.axf_movile.models.Reporte
import com.example.axf_movile.network.SessionManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout

class ReportesFragment : Fragment() {

    private var _binding: FragmentReportesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportesViewModel by activityViewModels()

    private lateinit var adapterPublicos: ReportesAdapter
    private lateinit var adapterMios: ReportesAdapter

    private var tabActual = 0  // 0 = Activos, 1 = Mis Reportes

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupTabs()
        setupObservers()
        setupListeners()

        // Carga inicial
        viewModel.loadReportesPublicos()
        viewModel.loadMisReportes()
    }

    private fun setupAdapters() {
        adapterPublicos = ReportesAdapter { reporte -> abrirDetalle(reporte) }
        adapterMios     = ReportesAdapter { reporte -> abrirDetalle(reporte) }

        binding.rvReportes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReportes.adapter = adapterPublicos
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tabActual = tab.position
                actualizarLista()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupObservers() {
        viewModel.reportesPublicos.observe(viewLifecycleOwner) { lista ->
            if (tabActual == 0) mostrarLista(lista, adapterPublicos)
        }

        viewModel.misReportes.observe(viewLifecycleOwner) { lista ->
            if (tabActual == 1) mostrarLista(lista, adapterMios)
        }

        viewModel.isLoadingList.observe(viewLifecycleOwner) { loading ->
            binding.shimmerReportes.apply {
                if (loading) { startShimmer(); visibility = View.VISIBLE }
                else         { stopShimmer();  visibility = View.GONE   }
            }
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (state is ReportesUiState.Error) {
                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                viewModel.resetUiState()
            }
        }
    }

    private fun setupListeners() {
        binding.fabNuevoReporte.setOnClickListener {
            if (!SessionManager.isActivo()) {
                Snackbar.make(
                    binding.root,
                    "Necesitas una suscripción activa para reportar",
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            findNavController().navigate(R.id.action_reportes_to_nuevoReporte)
        }

        binding.swipeRefresh.setColorSchemeColors(
            requireContext().getColor(R.color.orange_primary)
        )
        binding.swipeRefresh.setOnRefreshListener {
            if (tabActual == 0) viewModel.loadReportesPublicos()
            else viewModel.loadMisReportes()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun actualizarLista() {
        when (tabActual) {
            0 -> {
                binding.rvReportes.adapter = adapterPublicos
                mostrarLista(viewModel.reportesPublicos.value ?: emptyList(), adapterPublicos)
                binding.tvEmptySubtitle.text = "Sé el primero en reportar\nuna incidencia"
            }
            1 -> {
                binding.rvReportes.adapter = adapterMios
                mostrarLista(viewModel.misReportes.value ?: emptyList(), adapterMios)
                binding.tvEmptySubtitle.text = "No has hecho ningún reporte aún"
            }
        }
    }

    private fun mostrarLista(lista: List<Reporte>, adapter: ReportesAdapter) {
        adapter.submitList(lista)
        if (lista.isEmpty()) {
            binding.layoutEmpty.visibility  = View.VISIBLE
            binding.rvReportes.visibility   = View.GONE
        } else {
            binding.layoutEmpty.visibility  = View.GONE
            binding.rvReportes.visibility   = View.VISIBLE
        }
    }

    private fun abrirDetalle(reporte: Reporte) {
        val bundle = Bundle().apply { putInt("id_reporte", reporte.idReporte) }
        // Cargar en ViewModel para que DetalleFragment lo use directamente
        viewModel.loadReporteDetalle(reporte.idReporte)
        findNavController().navigate(R.id.action_reportes_to_detalleReporte, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
