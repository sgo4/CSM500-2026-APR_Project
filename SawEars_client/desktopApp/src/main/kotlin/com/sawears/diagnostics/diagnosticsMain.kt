package com.sawears.diagnostics

import com.sawears.SawEarsApp
import com.sawears.WindowsDiagnosticAppContainer
import com.sawears.data.database.seedDatabase
import kotlinx.coroutines.runBlocking

/**
 * Separate main function for diagnostics, using different DI container
 */
fun main() = runBlocking {

    // initialize app container
    val container = WindowsDiagnosticAppContainer()
    // seed database (if needed)
    seedDatabase(container.db)
    // launch app
    SawEarsApp(container)
}