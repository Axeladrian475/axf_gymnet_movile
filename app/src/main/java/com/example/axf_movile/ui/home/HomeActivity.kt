package com.example.axf_movile.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.axf_movile.R
import com.example.axf_movile.databinding.ActivityHomeBinding
import com.example.axf_movile.network.SessionManager
import com.example.axf_movile.ui.auth.LoginActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        checkNotifications()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        // Ocultar BottomNav en pantallas de detalle
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.rutinaDetalleFragment,
                R.id.dietaDetalleFragment,
                R.id.registroFisicoDetalleFragment,
                R.id.nuevoReporteFragment,
                R.id.reporteDetalleFragment,
                R.id.chatConversacionFragment -> {
                    binding.bottomNav.visibility = android.view.View.GONE
                }
                else -> {
                    binding.bottomNav.visibility = android.view.View.VISIBLE
                }
            }
        }
    }

    private fun checkNotifications() {
        // Verificar suscripción próxima a vencer
        // La lógica está en HomeFragment/ViewModel
    }

    fun logout() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Sí, salir") { _, _ ->
                SessionManager.clearSession()
                startActivity(Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onBackPressed() {
        if (!navController.navigateUp()) {
            super.onBackPressed()
        }
    }
}
