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
import com.papco.sundar.papcortgs.common.TextFunctions
import com.papco.sundar.papcortgs.dropbox.DropBoxAccount
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
        private val KEY_MESSAGE_TEMPLATE= stringPreferencesKey("message_template")
        private val KEY_DBX_USER_NAME= stringPreferencesKey("dbx_user_name")
        private val KEY_DBX_USER_EMAIL= stringPreferencesKey("dbx_user_email")
    }

    fun getLocalBackupFilePath():String{

        var dirPath = context.cacheDir.absolutePath
        dirPath = "$dirPath/Temp"

        val sdDir = File(dirPath)

        if (!sdDir.isDirectory) sdDir.mkdirs()

        return "$dirPath/papcortgsbackup.xls"
    }

    fun getMessageTemplate():Flow<String?> =
        dataStore.data.map {
            val format=it[KEY_MESSAGE_TEMPLATE]
            format ?: TextFunctions.getDefaultMessageFormat()
        }

    suspend fun saveMessageTemplate(template:String){
        dataStore.edit {
            it[KEY_MESSAGE_TEMPLATE]=template
        }
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
            it.remove(KEY_DBX_USER_NAME)
            it.remove(KEY_DBX_USER_EMAIL)
        }
    }

    suspend fun saveDropBoxAccount(account:DropBoxAccount){
        dataStore.edit {
            it[KEY_DBX_USER_NAME]=account.userName
            it[KEY_DBX_USER_EMAIL]=account.email
        }
    }

    fun getDropBoxAccount():Flow<DropBoxAccount?> =
        dataStore.data.map {
            val username = it[KEY_DBX_USER_NAME]
            val email = it[KEY_DBX_USER_EMAIL]

            if(username==null || email==null)
                null
            else
                DropBoxAccount(username,email)
        }
}