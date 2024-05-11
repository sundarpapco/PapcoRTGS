package com.papco.sundar.papcortgs.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dropbox.core.json.JsonReadException
import com.dropbox.core.oauth.DbxCredential
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

private const val PREFERENCE_NAME = "rtgs_preferences"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCE_NAME,
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, "${context.packageName}_preferences"))
    }
)

class AppPreferences(private val context: Context) {

    private val dataStore
        get() = context.dataStore


    companion object {
        private val KEY_DROPBOX_CREDENTIALS = stringPreferencesKey("key:dropbox_credentials")
    }

    fun getLocalBackupFilePath():String{

        var dirPath = context.cacheDir.absolutePath
        dirPath = "$dirPath/Temp"

        val sdDir = File(dirPath)

        if (!sdDir.isDirectory) sdDir.mkdirs()

        return "$dirPath/papcortgsbackup.xls"
    }

    fun getDropBoxCredentials():Flow<DbxCredential?> =
        dataStore.data.map {
            val serializedCredentials = it[KEY_DROPBOX_CREDENTIALS]
            if(serializedCredentials==null)
                null
            else{
                try{
                    DbxCredential.Reader.readFully(serializedCredentials)
                }catch(e:JsonReadException){
                    throw IllegalStateException("Saved Credential is Corrupt")
                }
            }
        }

    suspend fun saveDropBoxCredentials(credentials:DbxCredential){
        dataStore.edit {
            it[KEY_DROPBOX_CREDENTIALS]=credentials.toString()
        }
    }


    suspend fun clearDropBoxCredentials() {
        dataStore.edit {
            it.remove(KEY_DROPBOX_CREDENTIALS)
        }
    }
}