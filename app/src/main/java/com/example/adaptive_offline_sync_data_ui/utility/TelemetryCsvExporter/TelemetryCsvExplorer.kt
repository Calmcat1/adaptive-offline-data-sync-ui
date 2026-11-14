package com.example.adaptive_offline_sync_data_ui.utility.TelemetryCsvExporter

import android.content.Context
import com.example.adaptive_offline_sync_data_ui.data.entity.TelemetryEntity
import java.io.File
import java.io.FileWriter
import java.io.IOException

object TelemetryCsvExporter {
    private const val CSV_HEADER = "id,networkType,signalStrength,bandwidthKbps,latencyMs,batteryPercent,timestamp\n"

    /**
     * Converts a list of TelemetryEntity records to a CSV file.
     *
     * @param context Android Context to access internal/external cache directory
     * @param telemetryList The list of entities fetched from Room DB
     * @param fileName Name of the output file (default: telemetry_export.csv)
     * @return File object pointing to the generated CSV
     */
    fun exportToCsv(
        context: Context,
        telemetryList: List<TelemetryEntity>,
        fileName: String = "telemetry_export_${System.currentTimeMillis()}.csv"
    ): File? {
        val exportDir = File(context.cacheDir, "csv_exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }

        val csvFile = File(exportDir, fileName)

        return try {
            FileWriter(csvFile).use { writer ->
                // Write CSV Header
                writer.append(CSV_HEADER)

                // Write Data Rows
                for (item in telemetryList) {
                    writer.append("${item.id},")
                        .append("${sanitizeCsvField(item.networkType)},")
                        .append("${item.signalStrength},")
                        .append("${item.bandwidthKbps},")
                        .append("${item.latencyMs},")
                        .append("${item.batteryPercent},")
                        .append("${item.timestamp}\n")
                }
            }
            csvFile
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Prevents CSV injection/formatting breaks if networkType contains quotes or commas
     */
    private fun sanitizeCsvField(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}