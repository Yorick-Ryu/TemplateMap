package com.yorick.common.data.utils

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * 分享日志文件的结果状态
 */
sealed class ShareLogResult {
    /** 没有日志文件 */
    object NoLogs : ShareLogResult()

    /** 分享成功 */
    object Success : ShareLogResult()

    /** 分享失败 */
    data class Failed(val error: Throwable) : ShareLogResult()
}

object LogUtils {
    private const val LOG_DIR = "logs"
    private const val LOG_FILE_PREFIX = "uavsoso_"
    private const val LOG_FILE_EXTENSION = ".log"
    private const val MAX_LOG_FILES = 10
    private const val MAX_LOG_SIZE = 10 * 1024 * 1024 // 10MB

    fun initializeLogging(context: Context) {
        // Always plant debug tree for development
        Timber.plant(Timber.DebugTree())

        val fileTree = FileLoggingTree(context)
        Timber.plant(fileTree)
    }

    fun shareLogFiles(context: Context): ShareLogResult {
        val logFiles = getLogFiles(context)
        if (logFiles.isEmpty()) {
            Timber.w("No log files found to share")
            return ShareLogResult.NoLogs
        }

        val zipFile = createZipFromFiles(context, logFiles)
        if (zipFile == null) {
            return ShareLogResult.Failed(IOException("Failed to create zip file"))
        }

        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                zipFile
            )

            // 改为ACTION_SEND，因为我们只分享一个zip文件
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "UAVSOSO应用日志文件")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "来自UAVSOSO应用的日志文件（已压缩），包含应用运行日志和调试信息"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                // 添加额外的flag确保兼容性
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "选择分享方式")
            // 确保chooser也有NEW_TASK flag
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            Timber.i("Successfully launched share intent for log zip file: ${zipFile.name}")
            return ShareLogResult.Success
        } catch (e: Exception) {
            Timber.e(e, "Failed to launch share intent")
            zipFile.delete() // 如果分享失败，删除zip文件
            return ShareLogResult.Failed(e)
        }
    }

    private fun createZipFromFiles(context: Context, files: List<File>): File? {
        if (files.isEmpty()) {
            return null
        }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val zipFileName = "${LOG_FILE_PREFIX}logs_$timestamp.zip"
        val zipFile = File(context.cacheDir, zipFileName)

        try {
            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                files.forEach { file ->
                    if (file.exists() && file.canRead()) {
                        FileInputStream(file).use { fis ->
                            BufferedInputStream(fis).use { bis ->
                                val entry = ZipEntry(file.name)
                                zos.putNextEntry(entry)
                                bis.copyTo(zos, 1024)
                                zos.closeEntry()
                            }
                        }
                    } else {
                        Timber.w("Log file not found or not readable, skipping: ${file.path}")
                    }
                }
            }
            Timber.d("Created zip file: ${zipFile.path} with size ${zipFile.length()} bytes")
            return zipFile
        } catch (e: IOException) {
            Timber.e(e, "Failed to create zip file at ${zipFile.path}")
            zipFile.delete() // Clean up partially created zip file
            return null
        }
    }

    fun getLogFiles(context: Context): List<File> {
        val logDir = getLogDirectory(context)
        return if (logDir.exists()) {
            logDir.listFiles { _, name ->
                name.startsWith(LOG_FILE_PREFIX) && name.endsWith(LOG_FILE_EXTENSION)
            }?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun clearLogFiles(context: Context): Boolean {
        val logFiles = getLogFiles(context)
        if (logFiles.isEmpty()) {
            Timber.i("No log files to clear")

            return true // 没有文件也算成功
        }

        var successCount = 0
        var failCount = 0
        val totalCount = logFiles.size

        logFiles.forEach { file ->
            try {
                if (file.delete()) {
                    successCount++
//                    Timber.d("Successfully deleted log file: ${file.name}")
                } else {
                    failCount++
                    Timber.w("Failed to delete log file (delete returned false): ${file.name}")
                }
            } catch (e: Exception) {
                failCount++
                Timber.e(e, "Exception while deleting log file: ${file.name}")
            }
        }

        val isAllSuccess = failCount == 0
        if (isAllSuccess) {
//            Timber.i("Successfully cleared all $totalCount log files")
        } else {
            Timber.w("Log file clearing completed: $successCount success, $failCount failed out of $totalCount files")
        }

        return isAllSuccess
    }

    private fun getLogDirectory(context: Context): File {
        val logDir = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            File(context.getExternalFilesDir(null), LOG_DIR)
        } else {
            File(context.filesDir, LOG_DIR)
        }

        if (!logDir.exists()) {
            logDir.mkdirs()
        }

        return logDir
    }

    private class FileLoggingTree(private val context: Context) : Timber.Tree() {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        private val fileNameFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            try {
                val logFile = getCurrentLogFile()
                if (logFile.length() > MAX_LOG_SIZE) {
                    rotateLogFiles()
                }

                val timestamp = dateFormat.format(Date())
                val priorityStr = when (priority) {
                    Log.VERBOSE -> "V"
                    Log.DEBUG -> "D"
                    Log.INFO -> "I"
                    Log.WARN -> "W"
                    Log.ERROR -> "E"
                    Log.ASSERT -> "A"
                    else -> "?"
                }

                val logEntry = buildString {
                    append("$timestamp $priorityStr")
                    if (tag != null) {
                        append("/$tag")
                    }
                    append(": $message")
                    if (t != null) {
                        append("\n")
                        append(Log.getStackTraceString(t))
                    }
                    append("\n")
                }

                FileWriter(logFile, true).use { writer ->
                    writer.write(logEntry)
                    writer.flush()
                }

            } catch (e: IOException) {
                // Avoid infinite loop by not using Timber here
                Log.e("FileLoggingTree", "Failed to write log to file", e)
            }
        }

        private fun getCurrentLogFile(): File {
            val logDir = getLogDirectory(context)
            val fileName = "${LOG_FILE_PREFIX}${fileNameFormat.format(Date())}${LOG_FILE_EXTENSION}"
            return File(logDir, fileName)
        }

        private fun rotateLogFiles() {
            val logFiles = getLogFiles(context)
                .sortedByDescending { it.lastModified() }

            if (logFiles.size >= MAX_LOG_FILES) {
                logFiles.drop(MAX_LOG_FILES - 1).forEach { file ->
                    try {
                        file.delete()
                    } catch (e: Exception) {
                        Log.e("FileLoggingTree", "Failed to delete old log file", e)
                    }
                }
            }
        }
    }
}