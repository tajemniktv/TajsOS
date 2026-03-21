package com.tajemniktv.tajsos

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.tajemniktv.tajsos.data.createDatabase
import com.tajemniktv.tajsos.di.SharedModule
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File

fun main() = application {
    // Basic setup for Desktop
    val database = createDatabase()
    
    // Simple DataStore setup for Desktop
    val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { File(System.getProperty("user.home"), "tajsos.preferences_pb") }
    )
    
    val sharedModule = SharedModule(database, dataStore)
    val viewModel = sharedModule.createViewModel()

    Window(
        onCloseRequest = ::exitApplication,
        title = "TajsOS",
    ) {
        App(viewModel)
    }
}
