package com.example.sfulounge

import android.app.Application
import com.example.sfulounge.data.LoginDataSource
import com.example.sfulounge.data.LoginRepository
import com.example.sfulounge.data.MainRepository

class MainApplication : Application() {
    val loginRepository: LoginRepository by lazy { LoginRepository(LoginDataSource()) }
    val repository: MainRepository by lazy { MainRepository(loginRepository.user) }
}