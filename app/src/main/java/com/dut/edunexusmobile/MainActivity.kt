package com.dut.edunexusmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.dut.edunexusmobile.ui.navigation.EduNexusNavGraph
import com.dut.edunexusmobile.ui.theme.EduNexusMobileTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            EduNexusMobileTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EduNexusNavGraph()
                }
            }
        }
    }
}