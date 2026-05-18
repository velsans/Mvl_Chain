package com.tadamaps.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tadamaps.mobile.di.MviViewModelFactory
import javax.inject.Inject

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var viewModelFactory: MviViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        val app = application as MvlApplication
        app.applicationComponent
            .mainActivitySubcomponentFactory()
            .create(this)
            .inject(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MVLApp(viewModelFactory = viewModelFactory)
        }
    }
}
