package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.navigation.VibraTraceAppRoot
import com.example.ui.theme.VibraTraceTheme
import com.example.ui.viewmodel.VibraTraceViewModel
import com.example.ui.viewmodel.VibraTraceViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: VibraTraceViewModel by viewModels {
        VibraTraceViewModelFactory((application as VibraTraceApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VibraTraceTheme {
                VibraTraceAppRoot(viewModel = viewModel)
            }
        }
    }
}
