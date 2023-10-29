package com.example.sfulounge.ui.setup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.sfulounge.MainActivity
import com.example.sfulounge.R
import com.example.sfulounge.databinding.ActivitySetupDepthQuestionsBinding
import com.example.sfulounge.databinding.ActivitySetupInterestsBinding

class SetupDepthQuestionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetupDepthQuestionsBinding
    private lateinit var launcher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySetupDepthQuestionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val next = binding.next

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                finish()
            }
        }

        next.setOnClickListener {
            launcher.launch(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}