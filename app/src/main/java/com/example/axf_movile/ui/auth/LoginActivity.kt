package com.example.axf_movile.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.axf_movile.R
import com.example.axf_movile.databinding.ActivityLoginBinding
import com.example.axf_movile.network.SessionManager
import com.example.axf_movile.ui.home.HomeActivity
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si ya tiene sesión activa, ir directo al Home
        if (SessionManager.isLoggedIn()) {
            goToHome()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAnimations()
        setupObservers()
        setupListeners()
    }

    private fun setupAnimations() {
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        binding.logoLayout.startAnimation(fadeIn)
        binding.cardLogin.alpha = 0f
        binding.cardLogin.animate()
            .alpha(1f)
            .setDuration(600)
            .setStartDelay(300)
            .start()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val correo = binding.etCorreo.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (correo.isEmpty()) {
                binding.tilCorreo.error = "Ingresa tu correo"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.tilPassword.error = "Ingresa tu contraseña"
                return@setOnClickListener
            }

            binding.tilCorreo.error = null
            binding.tilPassword.error = null
            viewModel.login(correo, password)
        }
    }

    private fun setupObservers() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                    binding.btnLogin.text = ""
                }
                is LoginState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    goToHome()
                }
                is LoginState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = getString(R.string.iniciar_sesion)
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getColor(R.color.error))
                        .show()
                }
                is LoginState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = getString(R.string.iniciar_sesion)
                }
            }
        }
    }

    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
