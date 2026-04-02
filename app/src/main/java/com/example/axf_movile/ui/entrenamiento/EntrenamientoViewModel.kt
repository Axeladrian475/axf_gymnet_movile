package com.example.axf_movile.ui.entrenamiento

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.axf_movile.models.Rutina
import com.example.axf_movile.network.ApiClient
import com.example.axf_movile.network.SessionManager
import kotlinx.coroutines.launch

class EntrenamientoViewModel : ViewModel() {

    private val _rutinas = MutableLiveData<List<Rutina>>(emptyList())
    val rutinas: LiveData<List<Rutina>> = _rutinas

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _rutinaDetalle = MutableLiveData<Rutina?>()
    val rutinaDetalle: LiveData<Rutina?> = _rutinaDetalle

    fun loadRutinas() {
        val id = SessionManager.getIdSuscriptor()
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiClient.instance.getRutinasSuscriptor(id)
                if (response.isSuccessful) {
                    _rutinas.value = response.body() ?: emptyList()
                } else {
                    _error.value = "No se pudieron cargar las rutinas"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadRutinaDetalle(idRutina: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiClient.instance.getRutinaDetalle(idRutina)
                if (response.isSuccessful) {
                    _rutinaDetalle.value = response.body()
                } else {
                    _error.value = "No se pudo cargar la rutina"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
