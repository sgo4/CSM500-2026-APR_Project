package com.sawears.data.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

fun buildWindowsDatabase(): AppDatabase {
    val localAppData = System.getenv("LOCALAPPDATA")
        ?: File(System.getProperty("user.home"), "AppData/Local").absolutePath
    val appDataDir = File(localAppData,"Sawears")
        .apply { mkdirs() }
    val dbFile = File(appDataDir, "sawears.db")

    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    ).setDriver(BundledSQLiteDriver()).build()
}

