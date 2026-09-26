package com.hidrogeologo.campo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hidrogeologo.campo.ui.navigation.HidroCampoNavGraph
import com.hidrogeologo.campo.ui.theme.HidroCampoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HidroCampoAppRoot()
        }
    }
}

@Composable
private fun HidroCampoAppRoot() {
    HidroCampoTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            HidroCampoNavGraph()
        }
    }
}
