package com.example.sfulounge.ui.setup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.sfulounge.databinding.ActivitySetupBasicInfoBinding

class SetupBasicInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetupBasicInfoBinding
    private lateinit var launcher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySetupBasicInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val next = binding.next

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                finish()
            }
        }

        next.setOnClickListener {
            launcher.launch(Intent(this, SetupInterestsActivity::class.java))
        }
    }
}