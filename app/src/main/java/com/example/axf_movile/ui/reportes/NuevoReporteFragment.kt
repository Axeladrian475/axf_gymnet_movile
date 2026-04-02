package com.example.axf_movile.ui.reportes

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.axf_movile.R
import com.example.axf_movile.databinding.FragmentNuevoReporteBinding
import com.example.axf_movile.models.Personal
import com.example.axf_movile.models.Sucursal
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NuevoReporteFragment : Fragment() {

    private var _binding: FragmentNuevoReporteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportesViewModel by activityViewModels()

    // Listas para dropdowns
    private val categorias = listOf(
        "Máquina Dañada"       to "Maquina_Dañada",
        "Baño Tapado"          to "Baño_Tapado",
        "Problema de Limpieza" to "Problema_Limpieza",
        "Reporte de Personal"  to "Reporte_Personal",
        "Otro"                 to "Otro"
    )

    private var categoriaSeleccionada: String? = null  // valor real (DB)
    private var fotoUri: Uri? = null
    private var fotoFile: File? = null

    // ─── Launchers para cámara y galería ─────────────────────────────────────

    private val launcherCamara = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && fotoUri != null) {
            mostrarFotoPreview(fotoUri!!)
            viewModel.fotoUri  = fotoUri
            viewModel.fotoFile = fotoFile
        }
    }

    private val launcherGaleria = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            fotoUri = it
            // Copiar a cache para tener un File real para Multipart
            val file = crearArchivoTemp()
            requireContext().contentResolver.openInputStream(it)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            fotoFile = file
            viewModel.fotoUri  = fotoUri
            viewModel.fotoFile = fotoFile
            mostrarFotoPreview(it)
        }
    }

    private val launcherPermisos = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        if (permisos.values.all { it }) mostrarDialogFoto()
        else Snackbar.make(binding.root, "Permiso de cámara denegado", Snackbar.LENGTH_SHORT).show()
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNuevoReporteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupDropdownCategorias()
        setupObservers()
        setupListeners()

        viewModel.loadSucursales()
    }

    // ─── Setup ───────────────────────────────────────────────────────────────

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupDropdownCategorias() {
        val labels = categorias.map { it.first }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            labels
        )
        binding.actvCategoria.setAdapter(adapter)
        binding.actvCategoria.setOnItemClickListener { _, _, position, _ ->
            categoriaSeleccionada = categorias[position].second
            // Mostrar/ocultar sección de personal
            val esPersonal = categoriaSeleccionada == "Reporte_Personal"
            binding.layoutPersonal.visibility = if (esPersonal) View.VISIBLE else View.GONE
            // Si es reporte de personal, cargar personal de la sucursal seleccionada
            if (esPersonal && viewModel.sucursalSeleccionada != null) {
                viewModel.loadPersonalDeSucursal(viewModel.sucursalSeleccionada!!.idSucursal)
            }
        }
    }

    private fun setupObservers() {
        // Sucursales
        viewModel.sucursales.observe(viewLifecycleOwner) { lista ->
            if (lista.isEmpty()) return@observe
            val nombres = lista.map { it.nombre }
            val adapterSuc = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                nombres
            )
            binding.actvSucursal.setAdapter(adapterSuc)
            binding.actvSucursal.setOnItemClickListener { _, _, pos, _ ->
                viewModel.sucursalSeleccionada = lista[pos]
                // Si ya se seleccionó "Reporte de Personal", cargar personal
                if (categoriaSeleccionada == "Reporte_Personal") {
                    viewModel.loadPersonalDeSucursal(lista[pos].idSucursal)
                }
            }
        }

        // Personal para reportar
        viewModel.personal.observe(viewLifecycleOwner) { lista ->
            if (lista.isEmpty()) return@observe
            val nombres = lista.map { p -> "${p.nombres} ${p.apellidoPaterno} — ${formatPuesto(p.puesto)}" }
            val adapterPers = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                nombres
            )
            binding.actvPersonal.setAdapter(adapterPers)
            binding.actvPersonal.setOnItemClickListener { _, _, pos, _ ->
                viewModel.personalSeleccionado = lista[pos]
            }
        }

        // Estado UI
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ReportesUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnEnviar.isEnabled    = false
                    binding.btnEnviar.text         = ""
                }
                is ReportesUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                    viewModel.resetUiState()
                    findNavController().navigateUp()
                }
                is ReportesUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnEnviar.isEnabled    = true
                    binding.btnEnviar.text         = "Enviar reporte"
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(requireContext().getColor(R.color.error))
                        .show()
                    viewModel.resetUiState()
                }
                else -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnEnviar.isEnabled    = true
                    binding.btnEnviar.text         = "Enviar reporte"
                }
            }
        }
    }

    private fun setupListeners() {
        // Área de foto
        binding.layoutAddFoto.setOnClickListener { solicitarPermisoFoto() }
        binding.ivFotoPreview.setOnClickListener { solicitarPermisoFoto() }

        // Quitar foto
        binding.btnQuitarFoto.setOnClickListener {
            fotoUri  = null
            fotoFile = null
            viewModel.fotoUri  = null
            viewModel.fotoFile = null
            binding.ivFotoPreview.visibility = View.GONE
            binding.layoutAddFoto.visibility = View.VISIBLE
            binding.btnQuitarFoto.visibility = View.GONE
        }

        // Enviar
        binding.btnEnviar.setOnClickListener { validarYEnviar() }
    }

    // ─── Lógica de envío ─────────────────────────────────────────────────────

    private fun validarYEnviar() {
        val sucursal = viewModel.sucursalSeleccionada
        val categoria = categoriaSeleccionada
        val descripcion = binding.etDescripcion.text?.toString()?.trim() ?: ""

        // Validaciones
        if (sucursal == null) {
            binding.tilSucursal.error = "Selecciona una sucursal"
            return
        }
        binding.tilSucursal.error = null

        if (categoria == null) {
            binding.tilCategoria.error = "Selecciona una categoría"
            return
        }
        binding.tilCategoria.error = null

        if (descripcion.isEmpty()) {
            binding.tilDescripcion.error = getString(R.string.campo_requerido)
            return
        }
        binding.tilDescripcion.error = null

        // Si es reporte de personal, verificar que se seleccionó alguien
        if (categoria == "Reporte_Personal" && viewModel.personalSeleccionado == null) {
            binding.tilPersonal.error = "Selecciona al miembro del personal"
            return
        }
        binding.tilPersonal.error = null

        val esPrivado          = binding.switchPrivado.isChecked
        val idPersonal         = viewModel.personalSeleccionado?.idPersonal
        val sobreAtencionPrevia = if (categoria == "Reporte_Personal")
            binding.cbSobreAtencion.isChecked else null

        viewModel.crearReporte(
            idSucursal           = sucursal.idSucursal,
            categoria            = categoria,
            descripcion          = descripcion,
            esPrivado            = esPrivado,
            idPersonalReportado  = idPersonal,
            sobreAtencionPrevia  = sobreAtencionPrevia
        )
    }

    // ─── Foto ─────────────────────────────────────────────────────────────────

    private fun solicitarPermisoFoto() {
        val permisos = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED) {
            permisos.add(Manifest.permission.CAMERA)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {
                permisos.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permisos.isEmpty()) {
            mostrarDialogFoto()
        } else {
            launcherPermisos.launch(permisos.toTypedArray())
        }
    }

    private fun mostrarDialogFoto() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Agregar foto")
            .setItems(arrayOf("📷 Tomar foto", "🖼️ Elegir de galería")) { _, which ->
                if (which == 0) abrirCamara() else launcherGaleria.launch("image/*")
            }
            .show()
    }

    private fun abrirCamara() {
        fotoFile = crearArchivoTemp()
        fotoUri  = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            fotoFile!!
        )
        launcherCamara.launch(fotoUri)
    }

    private fun crearArchivoTemp(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: requireContext().cacheDir
        return File(dir, "reporte_$timestamp.jpg")
    }

    private fun mostrarFotoPreview(uri: Uri) {
        binding.layoutAddFoto.visibility = View.GONE
        binding.ivFotoPreview.visibility = View.VISIBLE
        binding.btnQuitarFoto.visibility = View.VISIBLE
        Glide.with(this).load(uri).centerCrop().into(binding.ivFotoPreview)
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun formatPuesto(puesto: String) = when (puesto) {
        "staff"                -> "Staff"
        "entrenador"           -> "Entrenador"
        "nutriologo"           -> "Nutriólogo"
        "entrenador_nutriologo"-> "Entrenador / Nutriólogo"
        else                   -> puesto
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
