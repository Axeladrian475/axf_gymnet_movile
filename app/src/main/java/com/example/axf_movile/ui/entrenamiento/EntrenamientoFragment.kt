package com.example.axf_movile.ui.entrenamiento

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.axf_movile.R
import com.example.axf_movile.databinding.FragmentEntrenamientoBinding
import com.example.axf_movile.models.Rutina
import com.example.axf_movile.network.SessionManager
import com.google.android.material.snackbar.Snackbar

class EntrenamientoFragment : Fragment() {

    private var _binding: FragmentEntrenamientoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EntrenamientoViewModel by viewModels()
    private lateinit var adapter: RutinasAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEntrenamientoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!SessionManager.isActivo()) {
            binding.layoutSinSuscripcion.visibility = View.VISIBLE
            binding.rvRutinas.visibility = View.GONE
            return
        }

        setupRecyclerView()
        setupObservers()

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadRutinas()
        }

        viewModel.loadRutinas()
    }

    private fun setupRecyclerView() {
        adapter = RutinasAdapter { rutina ->
            val bundle = Bundle().apply {
                putInt("id_rutina", rutina.idRutina)
            }
            findNavController().navigate(R.id.action_entrenamiento_to_rutinaDetalle, bundle)
        }
        binding.rvRutinas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRutinas.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.rutinas.observe(viewLifecycleOwner) { rutinas ->
            binding.swipeRefresh.isRefreshing = false
            if (rutinas.isEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rvRutinas.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.rvRutinas.visibility = View.VISIBLE
                adapter.submitList(rutinas)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.shimmerRutinas.apply {
                if (loading) { startShimmer(); visibility = View.VISIBLE }
                else { stopShimmer(); visibility = View.GONE }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty()) {
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
