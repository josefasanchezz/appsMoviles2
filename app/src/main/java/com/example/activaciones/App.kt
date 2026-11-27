package com.example.activaciones

import android.app.Application
import com.example.activaciones.data.AppDatabase
import com.example.activaciones.data.Repository
import com.example.activaciones.data.UserEntity

/** Sesión en memoria: usuario autenticado actual. */
class Session { var user: UserEntity? = null }

class App : Application() {
    lateinit var repository: Repository
        private set
    val session = Session()

    override fun onCreate() {
        super.onCreate()
        repository = Repository(AppDatabase.build(this))
    }
}
