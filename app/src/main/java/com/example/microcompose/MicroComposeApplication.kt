// app/src/main/java/com/example/microcompose/MicroComposeApplication.kt
package com.example.microcompose

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp // <-- This annotation initializes Hilt for the application
class MicroComposeApplication : Application() {
    // Usually, you don't need to add anything else here for basic Hilt setup.
    // You can override onCreate() for other app-wide initializations if needed.
    // override fun onCreate() {
    //     super.onCreate()
    //     // Other init code
    // }
}