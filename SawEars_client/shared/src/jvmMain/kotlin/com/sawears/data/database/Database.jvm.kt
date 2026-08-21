package com.sawears.data.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

fun buildWindowsDatabase(): AppDatabase {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "sawears.db")
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    ).setDriver(BundledSQLiteDriver()).build()
}

