package com.example.axf_movile.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.axf_movile.models.LoginRequest
import com.example.axf_movile.network.ApiClient
import com.example.axf_movile.network.SessionManager
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    val loginState: LiveData<LoginState> = _loginState

    fun login(correo: String, password: String) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            try {
                val response = ApiClient.instance.loginSuscriptor(
                    LoginRequest(correo, password)
                )

                if (response.isSuccessful) {
                    val body = response.body()!!
                    val user = body.user

                    SessionManager.saveSession(
                        token = body.token,
                        idSuscriptor = user.idSuscriptor,
                        nombre = user.nombre,
                        correo = user.correo,
                        idSucursal = user.idSucursalRegistro,
                        puntos = user.puntos,
                        rachaDias = user.rachaDias,
                        activo = user.activo
                    )

                    _loginState.value = LoginState.Success
                } else {
                    val msg = when (response.code()) {
                        401 -> "Correo o contraseña incorrectos"
                        404 -> "Suscriptor no encontrado"
                        else -> "Error al iniciar sesión (${response.code()})"
                    }
                    _loginState.value = LoginState.Error(msg)
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(
                    "No se pudo conectar al servidor. Verifica tu conexión."
                )
            }
        }
    }
}
