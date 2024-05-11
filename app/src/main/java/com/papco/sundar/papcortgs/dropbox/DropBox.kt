@file:Suppress("BlockingMethodInNonBlockingContext")

package com.example.payroll.dropbox

import android.content.Context
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.util.IOUtil
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.GetMetadataErrorException
import com.dropbox.core.v2.files.WriteMode
import com.papco.sundar.papcortgs.settings.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream

class DropBox(
    private val context: Context,
    private val appSettings: AppPreferences,
    private val dropBoxAppConfig: DropBoxAppConfig
) {

    companion object {
        const val DBX_BACKUP_FILE_PATH = "/apps/papcortgs/papcortgsbackup.xls"
    }

    suspend fun isConnected(): Boolean {

        if (appSettings.getDropBoxCredentials().first() != null) return true

        val mCredentials = Auth.getDbxCredential()
        if (mCredentials != null) {
            appSettings.saveDropBoxCredentials(mCredentials)
            return true
        }

        return false
    }

    suspend fun tryToConnect() {

        if (isConnected()) return

        startAuthorization()
    }

    private fun startAuthorization() {

        val requestConfig = DbxRequestConfig(dropBoxAppConfig.clientIdentifier)
        Auth.startOAuth2PKCE(context, dropBoxAppConfig.appKey, requestConfig)

    }

    suspend fun disConnect() {
        appSettings.clearDropBoxCredentials()
    }

    suspend fun downloadBackupFiles() {

        //Download the preference backup file
        downloadFile(
            DBX_BACKUP_FILE_PATH, appSettings.getLocalBackupFilePath(), "backup"
        )

    }


    private suspend fun downloadFile(
        remotePath: String, localPath: String, description: String = ""
    ): FileMetadata? {

        var metadata: FileMetadata? = null
        val clientV2 = initDropBoxClient()
        withContext(Dispatchers.IO) {

            try {
                metadata = (clientV2.files().getMetadata(remotePath)) as FileMetadata
            } catch (e: GetMetadataErrorException) {
                if (e.errorValue.isPath && e.errorValue.pathValue.isNotFound) throw FileNotFoundException(
                    "$description file not found"
                )
            }


            val outStream = FileOutputStream(File(localPath))
            clientV2.files().download(remotePath).download(outStream)

        }

        return metadata

    }


    @ExperimentalCoroutinesApi
    suspend fun uploadBackupFile(){

        val clientV2 = initDropBoxClient()


        val fileToUpload = File(appSettings.getLocalBackupFilePath())
        require(fileToUpload.exists()) { "Backup file does not exists anymore" }

        val inputStream = FileInputStream(fileToUpload)

        val progressListener = IOUtil.ProgressListener {
            //Do Nothing
        }

        clientV2.files().uploadBuilder(DBX_BACKUP_FILE_PATH).withMode(WriteMode.OVERWRITE)
            .uploadAndFinish(inputStream, progressListener)

    }


    private suspend fun initDropBoxClient(): DbxClientV2 {

        val savedCredential = appSettings.getDropBoxCredentials().first()
        require(savedCredential != null) { "Cannot initialize DropBox client without valid saved credentials" }
        val credential = DbxCredential(
            savedCredential.accessToken, -1L, savedCredential.refreshToken, savedCredential.appKey
        )

        val dbxConfig = DbxRequestConfig(dropBoxAppConfig.clientIdentifier)

        return DbxClientV2(dbxConfig, credential)
    }


}