package com.example.axf_movile.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.axf_movile.models.SuscripcionActiva
import com.example.axf_movile.network.ApiClient
import com.example.axf_movile.network.SessionManager
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _suscripcion = MutableLiveData<SuscripcionActiva?>()
    val suscripcion: LiveData<SuscripcionActiva?> = _suscripcion

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadData() {
        val id = SessionManager.getIdSuscriptor()
        if (id == -1) return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiClient.instance.getSuscripcionActiva(id)
                _suscripcion.value = if (response.isSuccessful) response.body() else null
            } catch (e: Exception) {
                _suscripcion.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
}
