package com.trkibo.google_sign_in_native

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** GoogleSignInNativePlugin */
class GoogleSignInNativePlugin :
    FlutterPlugin,
    MethodCallHandler,
    ActivityAware {
    private lateinit var channel: MethodChannel

    private var utils: GoogleSignInNativeUtils = GoogleSignInNativeUtils()
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private lateinit var context: Context
    private var activity: Activity? = null

    // Use currentContext property to get the current context (activity or application context)
    private val currentContext get() = activity ?: context

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "google_sign_in_native")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.methos) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }

            "init" -> handleInitMethod(call, result)
            else -> handleMainMethods(call, result)
        }
    }

    private fun handleMainMethods(call: MethodCall, result: Result) {
        mainScope.launch {
            withContext(Dispatchers.Main) {
                try {
                    when (call.method) {
                        "google_sign_in" -> handleGoogleSignIn(call, result)
                        "logout" -> handleLogout(result)
                        else -> result.notImplemented()

                    }
                } catch (e: Exception) {
                    result.error("204", "Login failed", e.localizedMessage)
                }
            }
        }
    }

    private fun handleInitMethod(call: MethodCall, result: Result) {
        val preferImmediatelyAvailableCredentials: Boolean =
            call.argument("prefer_immediately_available_credentials") ?: true
        val googleClientId: String? = call.argument("google_client_id")

        val (exception: GoogleSignInNativeExceptions?, message: String) =
            utils.initialize(preferImmediatelyAvailableCredentials, googleClientId, currentContext)

        if (exception != null) {
            result.error(exception.code.toString(), exception.message, exception.details)
        } else {
            result.success(message)
        }
    }

    private suspend fun handleGoogleSignIn(call: MethodCall, result: Result) {
        val useButtonFlow: Boolean = call.argument("useButtonFlow") ?: false
        val scopes: List<String>? = call.argument("scopes")

        val (exception: GoogleSignInNativeExceptions?, credential: GoogleIdTokenCredential?) =
            utils.saveGoogleCredentials(
                useButtonFlow = useButtonFlow,
                scopes = scopes,
                context = currentContext
            )

        if (exception != null) {
            result.error(exception.code.toString(), exception.message, exception.details)
        } else {
            val credentialMap = mapOf(
                "id" to credential?.id,
                "idToken" to credential?.idToken,
                "displayName" to credential?.displayName,
                "givenName" to credential?.givenName,
                "familyName" to credential?.familyName,
                "phoneNumber" to credential?.phoneNumber,
                "profilePictureUri" to credential?.profilePictureUri.toString()
            )

            result.success(credentialMap)
        }
    }

    private suspend fun handleLogout(result: Result) {
        val (exception: GoogleSignInNativeExceptions?, message: String) = utils.logout()
        if (exception != null) {
            result.error(exception.code.toString(), exception.message, exception.details)
        } else {
            result.success(message)
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(p0: ActivityPluginBinding) {
        activity = p0.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(p0: ActivityPluginBinding) {
        activity = p0.activity
    }

    override fun onDetachedFromActivity() {
        activity = null
    }
}
