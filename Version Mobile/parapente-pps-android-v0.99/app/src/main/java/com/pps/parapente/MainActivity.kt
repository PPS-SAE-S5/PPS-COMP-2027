package com.pps.parapente

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pps.parapente.data.AppDatabase
import com.pps.parapente.data.Repository
import com.pps.parapente.sync.SyncConfig
import com.pps.parapente.ui.PpsApp
import com.pps.parapente.ui.PpsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.get(this)
        val repo = Repository(db)
        val syncConfig = SyncConfig(this)
        setContent {
            PpsTheme {
                PpsApp(repo, db, syncConfig)
            }
        }
    }
}
