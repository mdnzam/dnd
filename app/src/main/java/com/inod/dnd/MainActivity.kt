package com.inod.dnd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.inod.dnd.ui.DndScreen
import com.inod.dnd.ui.theme.DndTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DndController.init(this)
        enableEdgeToEdge()
        setContent {
            DndTheme {
                AppScaffold()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Periksa ulang izin/status setelah kembali dari layar pengaturan.
        DndController.refresh()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScaffold() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Do Not Disturb") })
        }
    ) { innerPadding ->
        DndScreen(modifier = Modifier.padding(innerPadding))
    }
}
