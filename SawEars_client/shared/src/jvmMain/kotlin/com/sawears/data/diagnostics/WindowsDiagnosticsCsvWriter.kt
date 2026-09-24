package com.sawears.data.diagnostics

import com.sawears.diagnostics.RealTimeLatencyMark
import com.sawears.diagnostics.ResultsWriter
import java.io.BufferedWriter
import java.io.File

/**
 * Receive diagnostics results and write to csv file
 * Specific to Windows / JVM platform
 */
class WindowsDiagnosticsCsvWriter(
    val fileName: String
): ResultsWriter {

    private val writer: BufferedWriter

    init {
        require(fileName.endsWith(".csv")){
            "diagnostics file must be csv"
        }
        val documents = File(System.getProperty("user.home"), "Documents").absolutePath
        val appDataDir = File(documents,"Sawears")
            .apply { mkdirs() }
        val csvFile = File(appDataDir, fileName)
        writer = csvFile.bufferedWriter()
        writeHeader()
    }

    fun writeHeader() {
        writer.appendLine(
            "frame_id,stage,timestamp_nanos"
        )
    }

    override fun write(mark: RealTimeLatencyMark) {
        writer.append(mark.id.toString())
        writer.append(',')
        writer.append(mark.stage.name)
        writer.append(',')
        writer.appendLine(mark.timeStampNanos.toString())
    }

    override fun flush() {
        writer.flush()
    }

    override fun close() {
        writer.close()
    }

}