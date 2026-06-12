package com.papco.sundar.papcortgs.extentions

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

suspend fun Uri.copyToLocalBackupFile(
    context: Context,
    localFilePath: String
) =
    withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(this@copyToLocalBackupFile)?.use { inputStream ->
            val savingFile = File(localFilePath)
            savingFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }