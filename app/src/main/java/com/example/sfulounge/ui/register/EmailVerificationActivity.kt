package com.example.sfulounge.ui.register

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.sfulounge.R
import com.example.sfulounge.databinding.ActivityEmailVerificationBinding

class EmailVerificationActivity : AppCompatActivity() {

    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var binding: ActivityEmailVerificationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerViewModel = ViewModelProvider(this, RegisterModelFactory())
            .get(RegisterViewModel::class.java)

        registerViewModel.verificationResult.observe(this, Observer {
            val verResult = it ?: return@Observer

            if (verResult.error != null) {
                showVerificationFailed(verResult.error)
            } else {
                showVerificationEmailSent()
            }
        })

        val resend = binding.resendEmail
        val login = binding.login

        resend.setOnClickListener {
            registerViewModel.retrySendVerificationEmail()
        }

        login.setOnClickListener {
            finish()
        }
    }

    private fun showVerificationFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, getString(errorString), Toast.LENGTH_SHORT).show()
    }

    private fun showVerificationEmailSent() {
        Toast.makeText(this, "Verification email sent!", Toast.LENGTH_SHORT)
            .show()
    }
}