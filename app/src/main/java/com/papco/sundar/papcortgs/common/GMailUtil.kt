package com.papco.sundar.papcortgs.common

import android.content.Context
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.papco.sundar.papcortgs.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GMailUtil(private val context: Context) {

    val client by lazy{
        createGoogleClient(context)
    }

    private fun createGoogleClient(context:Context): GoogleSignInClient {
        val scopeSendMail = "https://www.googleapis.com/auth/gmail.send"
        val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        builder.requestScopes(Scope(scopeSendMail))
        builder.requestIdToken(context.getString(R.string.client_id))
        builder.requestEmail()
        builder.requestProfile()
        val gso = builder.build()
        return GoogleSignIn.getClient(context.applicationContext, gso)
    }

    fun isConnected():Boolean{
        return GoogleSignIn.getLastSignedInAccount(context.applicationContext) != null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun signOut() = suspendCancellableCoroutine { continuation->
        client.signOut().addOnCompleteListener {
            continuation.resume(true)
        }.addOnFailureListener {
            continuation.resumeWithException(it)
        }
    }
}