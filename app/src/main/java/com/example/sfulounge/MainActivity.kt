package com.example.sfulounge

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.sfulounge.databinding.ActivityMainBinding
import com.example.sfulounge.ui.setup.SetupBasicInfoActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_profile, R.id.navigation_explore, R.id.navigation_chats
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // if the user has not setup his/her profile info then start the Setup activity
        homeViewModel = ViewModelProvider(this, HomeViewModelFactory())
            .get(HomeViewModel::class.java)

        homeViewModel.userResult.observe(this, Observer {
            val user = it ?: return@Observer

            if (!user.isProfileInitialized) {
                startActivity(Intent(this, SetupBasicInfoActivity::class.java))
            }
        })

        homeViewModel.getUser()
    }
}