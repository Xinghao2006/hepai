package com.example.hepai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hepai.data.MainViewModel
import com.example.hepai.ui.HepaiApp
import com.example.hepai.ui.HepaiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HepaiTheme {
                val vm: MainViewModel = viewModel()
                HepaiApp(vm)
            }
        }
    }
}
