package com.trkibo.google_sign_in_native

import android.content.Context
import android.util.Log
import androidx.credentials.*
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

import com.google.android.gms.auth.api.identity.AuthorizationClient
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import androidx.activity.result.contract.ActivityResultContracts

import java.util.UUID
import java.security.MessageDigest

data class GoogleAuthorizationResult(
    val accessToken: String?,
    val serverAuthCode: String?,
    val grantedScopes: List<String>?,
    val error: GoogleSignInNativeExceptions?
)

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
     * Authorize additional Google scopes.
     *
     * @param scopes List of scopes to request (e.g., ["email", "profile"]).
     * @param requestOfflineAccess Whether to request a server auth code for offline access.
     * @param callback Callback to handle the authorization result.
     */
    fun authorizeGoogleScopes(
        scopes: List<String>,
        requestOfflineAccess: Boolean,
        callback: (GoogleAuthorizationResult) -> Unit
    ) {
        if (activity == null) {
            callback(
                GoogleAuthorizationResult(
                    accessToken = null,
                    serverAuthCode = null,
                    grantedScopes = null,
                    error = GoogleSignInNativeExceptions(
                        code = 601,
                        message = "Activity not available",
                        details = "Authorization requires an active Activity"
                    )
                )
            )
            return
        }

        if (!this::serverClientID.isInitialized) {
            callback(
                GoogleAuthorizationResult(
                    accessToken = null,
                    serverAuthCode = null,
                    grantedScopes = null,
                    error = GoogleSignInNativeExceptions(
                        code = 503,
                        message = "Google client is not initialized yet",
                        details = "Check if Google credentials is provided"
                    )
                )
            )
            return
        }

        // Convert string scopes to Google API Scope objects
        val requestedScopes = scopes.map { Scope(it) }
        val authorizationRequest = AuthorizationRequest.Builder()
            .setRequestedScopes(requestedScopes)
            .apply {
                if (requestOfflineAccess) {
                    requestOfflineAccess(serverClientID)
                }
            }
            .build()

        // Register ActivityResultLauncher
        val launcher = activity!!.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            try {
                val authorizationResult = AuthorizationClient.getAuthorizationResultFromIntent(result.data)
                callback(
                    GoogleAuthorizationResult(
                        accessToken = authorizationResult.accessToken,
                        serverAuthCode = authorizationResult.serverAuthCode,
                        grantedScopes = authorizationResult.grantedScopes?.map { it.scopeUri },
                        error = null
                    )
                )
            } catch (e: ApiException) {
                callback(
                    GoogleAuthorizationResult(
                        accessToken = null,
                        serverAuthCode = null,
                        grantedScopes = null,
                        error = GoogleSignInNativeExceptions(
                            code = 602,
                            message = "Authorization failed",
                            details = e.localizedMessage
                        )
                    )
                )
            }
        }

        // Start authorization
        AuthorizationClient.getAuthorizationClient(activity!!)
            .authorize(authorizationRequest)
            .addOnSuccessListener { authorizationResult ->
                if (authorizationResult.hasResolution()) {
                    // User needs to grant access
                    val pendingIntent = authorizationResult.pendingIntent
                    launcher.launch(
                        IntentSenderRequest.Builder(pendingIntent!!.intentSender).build()
                    )
                } else {
                    // Access already granted
                    callback(
                        GoogleAuthorizationResult(
                            accessToken = authorizationResult.accessToken,
                            serverAuthCode = authorizationResult.serverAuthCode,
                            grantedScopes = authorizationResult.grantedScopes?.map { it.scopeUri },
                            error = null
                        )
                    )
                }
            }
            .addOnFailureListener { e ->
                callback(
                    GoogleAuthorizationResult(
                        accessToken = null,
                        serverAuthCode = null,
                        grantedScopes = null,
                        error = GoogleSignInNativeExceptions(
                            code = 602,
                            message = "Authorization failed",
                            details = e.localizedMessage
                        )
                    )
                )
            }
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
