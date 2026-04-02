package com.example.axf_movile.ui.reportes

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.axf_movile.models.Personal
import com.example.axf_movile.models.Reporte
import com.example.axf_movile.models.Sucursal
import com.example.axf_movile.network.ApiClient
import com.example.axf_movile.network.SessionManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

// ─── Estados UI ───────────────────────────────────────────────────────────────

sealed class ReportesUiState {
    object Idle : ReportesUiState()
    object Loading : ReportesUiState()
    data class Success(val message: String) : ReportesUiState()
    data class Error(val message: String) : ReportesUiState()
}

class ReportesViewModel : ViewModel() {

    // Listas
    private val _reportesPublicos = MutableLiveData<List<Reporte>>(emptyList())
    val reportesPublicos: LiveData<List<Reporte>> = _reportesPublicos

    private val _misReportes = MutableLiveData<List<Reporte>>(emptyList())
    val misReportes: LiveData<List<Reporte>> = _misReportes

    private val _reporteDetalle = MutableLiveData<Reporte?>()
    val reporteDetalle: LiveData<Reporte?> = _reporteDetalle

    // Datos para formulario
    private val _sucursales = MutableLiveData<List<Sucursal>>(emptyList())
    val sucursales: LiveData<List<Sucursal>> = _sucursales

    private val _personal = MutableLiveData<List<Personal>>(emptyList())
    val personal: LiveData<List<Personal>> = _personal

    // Estado de UI
    private val _uiState = MutableLiveData<ReportesUiState>(ReportesUiState.Idle)
    val uiState: LiveData<ReportesUiState> = _uiState

    private val _isLoadingList = MutableLiveData(false)
    val isLoadingList: LiveData<Boolean> = _isLoadingList

    // Foto seleccionada
    var fotoUri: Uri? = null
    var fotoFile: File? = null

    // Sucursal seleccionada en el form
    var sucursalSeleccionada: Sucursal? = null
    var personalSeleccionado: Personal? = null

    // ─── Cargar reportes públicos de la sucursal del suscriptor ──────────────
    fun loadReportesPublicos() {
        val idSucursal = SessionManager.getIdSucursal()
        _isLoadingList.value = true
        viewModelScope.launch {
            try {
                val response = ApiClient.instance.getReportesPublicos(idSucursal)
                if (response.isSuccessful) {
                    _reportesPublicos.value = response.body() ?: emptyList()
                } else {
                    _reportesPublicos.value = emptyList()
                }
            } catch (e: Exception) {
                _reportesPublicos.value = emptyList()
            } finally {
                _isLoadingList.value = false
            }
        }
    }

    // ─── Cargar mis reportes ──────────────────────────────────────────────────
    fun loadMisReportes() {
        _isLoadingList.value = true
        viewModelScope.launch {
            try {
                val response = ApiClient.instance.getMisReportes()
                if (response.isSuccessful) {
                    _misReportes.value = response.body() ?: emptyList()
                } else {
                    _misReportes.value = emptyList()
                }
            } catch (e: Exception) {
                _misReportes.value = emptyList()
            } finally {
                _isLoadingList.value = false
            }
        }
    }

    // ─── Cargar detalle de reporte ────────────────────────────────────────────
    fun loadReporteDetalle(idReporte: Int) {
        _uiState.value = ReportesUiState.Loading
        viewModelScope.launch {
            try {
                // Buscar en la lista existente primero (más rápido)
                val existing = _reportesPublicos.value?.find { it.idReporte == idReporte }
                    ?: _misReportes.value?.find { it.idReporte == idReporte }
                if (existing != null) {
                    _reporteDetalle.value = existing
                    _uiState.value = ReportesUiState.Idle
                } else {
                    _uiState.value = ReportesUiState.Error("No se pudo cargar el reporte")
                }
            } catch (e: Exception) {
                _uiState.value = ReportesUiState.Error("Error de conexión")
            }
        }
    }

    // ─── Cargar sucursales para el formulario ─────────────────────────────────
    fun loadSucursales() {
        viewModelScope.launch {
            try {
                val response = ApiClient.instance.getSucursales()
                if (response.isSuccessful) {
                    _sucursales.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) { /* ignorar */ }
        }
    }

    // ─── Cargar personal de una sucursal para reportar personal ───────────────
    fun loadPersonalDeSucursal(idSucursal: Int) {
        viewModelScope.launch {
            try {
                val response = ApiClient.instance.getPersonalSucursal(idSucursal)
                if (response.isSuccessful) {
                    _personal.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) { /* ignorar */ }
        }
    }

    // ─── Crear reporte ────────────────────────────────────────────────────────
    fun crearReporte(
        idSucursal: Int,
        categoria: String,
        descripcion: String,
        esPrivado: Boolean,
        idPersonalReportado: Int?,
        sobreAtencionPrevia: Boolean?
    ) {
        if (descripcion.isBlank()) {
            _uiState.value = ReportesUiState.Error("La descripción es obligatoria")
            return
        }

        _uiState.value = ReportesUiState.Loading

        viewModelScope.launch {
            try {
                // Construir multipart
                val idSucursalBody = idSucursal.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val categoriaBody = categoria
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val descripcionBody = descripcion
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val esPrivadoBody = (if (esPrivado) "1" else "0")
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                val idPersonalBody = idPersonalReportado?.toString()
                    ?.toRequestBody("text/plain".toMediaTypeOrNull())

                val sobreAtencionBody = sobreAtencionPrevia?.let {
                    (if (it) "1" else "0").toRequestBody("text/plain".toMediaTypeOrNull())
                }

                // Foto opcional
                val fotoPart: MultipartBody.Part? = fotoFile?.let { file ->
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("foto", file.name, requestFile)
                }

                val response = ApiClient.instance.crearReporte(
                    idSucursalBody, categoriaBody, descripcionBody,
                    esPrivadoBody, idPersonalBody, sobreAtencionBody, fotoPart
                )

                if (response.isSuccessful) {
                    _uiState.value = ReportesUiState.Success("Reporte enviado correctamente")
                    // Limpiar estado de foto
                    fotoUri = null
                    fotoFile = null
                    // Refrescar listas
                    loadReportesPublicos()
                    loadMisReportes()
                } else {
                    val code = response.code()
                    val msg = when (code) {
                        400 -> "Datos incompletos. Verifica el formulario."
                        401 -> "Sesión expirada. Vuelve a iniciar sesión."
                        else -> "Error al enviar el reporte ($code)"
                    }
                    _uiState.value = ReportesUiState.Error(msg)
                }
            } catch (e: Exception) {
                _uiState.value = ReportesUiState.Error("Sin conexión al servidor")
            }
        }
    }

    // ─── Sumarse a un reporte ─────────────────────────────────────────────────
    fun sumarseReporte(idReporte: Int) {
        _uiState.value = ReportesUiState.Loading
        viewModelScope.launch {
            try {
                val response = ApiClient.instance.sumarseReporte(idReporte)
                if (response.isSuccessful) {
                    _uiState.value = ReportesUiState.Success("Te has sumado al reporte")
                    // Actualizar el reporte en la lista local
                    _reportesPublicos.value = _reportesPublicos.value?.map { r ->
                        if (r.idReporte == idReporte) {
                            r.copy(yaSumado = true, numSumados = (r.numSumados ?: 0) + 1)
                        } else r
                    }
                    loadReporteDetalle(idReporte)
                } else {
                    val msg = if (response.code() == 409)
                        "Ya estás sumado a este reporte"
                    else "No se pudo sumar al reporte"
                    _uiState.value = ReportesUiState.Error(msg)
                }
            } catch (e: Exception) {
                _uiState.value = ReportesUiState.Error("Error de conexión")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = ReportesUiState.Idle
    }
}
