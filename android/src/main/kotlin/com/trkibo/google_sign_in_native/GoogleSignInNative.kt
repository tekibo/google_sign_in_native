package com.trkibo.google_sign_in_native

import android.content.ContentValues
import android.content.Context
import android.credentials.GetCredentialException
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi

import androidx.credentials.*
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.delay
import java.security.SecureRandom
import java.util.Base64

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
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
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

        val result = if (useButtonFlow) {
            signInWithButton(context, webclientId = serverClientID)
        } else {
            signInAutomatic(context, webclientId = serverClientID)
        }

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

}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
suspend fun signInWithButton(
    context: Context,
    webclientId: String,
): GetCredentialResponse {
    val googleCredentialOption = GetSignInWithGoogleOption.Builder(webclientId)
        .setNonce(generateSecureRandomNonce())
        .build()
    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleCredentialOption)
        .build()
    val credentialManager = CredentialManager.create(context)

    val result = credentialManager.getCredential(
        request = request,
        context = context,
    )
    return result
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
suspend fun signInAutomatic(
    context: Context,
    webclientId: String,
): GetCredentialResponse {
    val request: GetCredentialRequest =
        getCredentials(firstTime = true, webclientId)
    val credentialManager = CredentialManager.create(context)

    delay(250)

    try {
        val result = credentialManager.getCredential(
            request = request,
            context = context,
        )
        return result
    } catch (e: NoCredentialException) {
        val requestFalse: GetCredentialRequest =
            getCredentials(firstTime = false, webclientId)
        val resultFalse = credentialManager.getCredential(
            request = requestFalse,
            context = context,
        )
        return resultFalse
    }

}

@RequiresApi(Build.VERSION_CODES.O)
fun getCredentials(
    firstTime: Boolean,
    webClientId: String
): GetCredentialRequest {
    val googleIdOption =
        GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(firstTime)
            .setServerClientId(webClientId)
            .setNonce(generateSecureRandomNonce())
            .build()

    return GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
}

@RequiresApi(Build.VERSION_CODES.O)
fun generateSecureRandomNonce(byteLength: Int = 32): String {
    val randomBytes = ByteArray(byteLength)
    SecureRandom.getInstanceStrong().nextBytes(randomBytes)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
}
