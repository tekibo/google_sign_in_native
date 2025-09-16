package com.trkibo.google_sign_in_native

import android.content.Context
import android.util.Log

import androidx.credentials.*
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

import androidx.activity.result.ActivityResult

import java.util.UUID
import java.security.MessageDigest

class GoogleSignInNativeUtils {
    private lateinit var credentialManager: CredentialManager
    private var preferImmediatelyAvailableCredentials: Boolean = true
    private lateinit var serverClientID: String

    fun initialize(
        preferImmediatelyAvailableCredentials: Boolean,
        gClientId: String?,
        context: Context,
    ): Pair<GoogleSignInNativeExceptions?, String> {
        return try {
            credentialManager = CredentialManager.create(context = context)
            this.preferImmediatelyAvailableCredentials = preferImmediatelyAvailableCredentials
            if (gClientId != null) {
                serverClientID = gClientId
            }
            Pair(null, "Initialization successful")
        } catch (e: Exception) {
            Log.d("GoogleSignInNative", "${e.message}")
            val message = e.localizedMessage
            Pair(
                GoogleSignInNativeExceptions(
                    code = 101,
                    message = "Initialization failure",
                    details = message
                ), ""
            )
        }
    }

    /**
     * Save Google credentials.
     *
     * @param context The Android context.
     * @return A Pair containing either null and deserialized GoogleIdTokenCredential
     * or GoogleSignInNativeExceptions and null if an error occurs.
     */
    suspend fun saveGoogleCredentials(
        useButtonFlow: Boolean,
        context: Context
    ): Pair<GoogleSignInNativeExceptions?, GoogleIdTokenCredential?> {
        if (!this::serverClientID.isInitialized) {
            return Pair(
                GoogleSignInNativeExceptions(
                    code = 503,
                    message = "Google client is not initialized yet",
                    details = "Check if Google credentials is provided"
                ), null
            )
        }

        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val nonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        val googleCredentialOption = if (useButtonFlow) {
            GetSignInWithGoogleOption.Builder(serverClientID)
                .setNonce(nonce)
                .build()
        } else {
            GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientID)
                .setAutoSelectEnabled(true)
                .setNonce(nonce)
                .build()
        }

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleCredentialOption)
            .build()

        Log.d("GoogleSignInNative", "$request")
        val result = credentialManager.getCredential(
            request = request,
            context = context,
        )

        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    return try {
                        // Use googleIdTokenCredential and extract id to validate and
                        // authenticate on your server.
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        Pair(null, googleIdTokenCredential)
                    } catch (e: GoogleIdTokenParsingException) {
                        Pair(
                            GoogleSignInNativeExceptions(
                                code = 501,
                                message = "Received an invalid google id token response",
                                details = e.localizedMessage,
                            ), null
                        )
                    }
                }
            }
        }
        return Pair(
            GoogleSignInNativeExceptions(
                code = 502,
                message = "Invalid request",
                details = null
            ), null
        )
    }


    /**
     * Logout the user.
     *
     * @return A Pair containing either null and a success message or GoogleSignInNativeExceptions and an empty string.
     */
    suspend fun logout(): Pair<GoogleSignInNativeExceptions?, String> {
        return try {
            credentialManager.clearCredentialState(
                ClearCredentialStateRequest()
            )
            Pair(null, "Logout successful")
        } catch (e: Exception) {
            Pair(
                GoogleSignInNativeExceptions(
                    code = 701,
                    message = "Logout failed",
                    details = e.localizedMessage
                ), ""
            )
        }
    }
}
