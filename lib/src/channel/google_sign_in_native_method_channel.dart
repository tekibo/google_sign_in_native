import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:google_sign_in_native/google_sign_in_native.dart';

/// An implementation of [GoogleSignInNativePlatform] that uses method channels.
class MethodChannelGoogleSignInNative extends GoogleSignInNativePlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('google_sign_in_native');

  /// Initializes the credential manager with optional preferences.
  @override
  Future<void> init(
    bool preferImmediatelyAvailableCredentials,
    String? googleClientId,
  ) async {
    final res = await methodChannel.invokeMethod<String>("init", {
      'prefer_immediately_available_credentials':
          preferImmediatelyAvailableCredentials,
      'google_client_id': googleClientId,
    });

    if (res != null && res == "Initialization successful") {
      return;
    }

    throw GoogleSignInNativeException(
      code: 101,
      message: "Initialization failure",
      details: null,
    );
  }

  /// Saves Google ID token credential.
  @override
  Future<GoogleIdTokenCredential?> googleSignIn(
    bool useButtonFlow) async {
    try {
      final res = await methodChannel.invokeMethod<Map<Object?, Object?>>(
        'google_sign_in',
        {"useButtonFlow": useButtonFlow},
      );

      if (res == null) {
        throw GoogleSignInNativeException(
          code: 505,
          message: "Google credential decode error",
          details: "Null response received",
        );
      }
      return GoogleIdTokenCredential.fromJson(jsonDecode(jsonEncode(res)));
    } on PlatformException catch (e) {
      throw handlePlatformException(e);
    }
  }

  /// Handles PlatformException and returns appropriate GoogleSignInNativeException based on error codes.
  ///
  /// This function maps error codes from the PlatformException to human-readable messages and returns
  /// a GoogleSignInNativeException. It covers various scenarios such as initialization failures, login issues,
  /// credential saving errors, encryption/decryption failures, and more.
  ///
  /// [e] - The PlatformException thrown.
  ///
  /// Returns a GoogleSignInNativeException with an appropriate error code and message.
  GoogleSignInNativeException handlePlatformException(PlatformException e) {
    switch (e.code) {
      case "101":
        return GoogleSignInNativeException(
          code: 101,
          message: "Initialization failure",
          details: e.details,
        );
      case "102":
        return GoogleSignInNativeException(
          code: 102,
          message: "Plugin exception",
          details: e.details,
        );
      case "103":
        return GoogleSignInNativeException(
          code: 103,
          message: "Not implemented",
          details: e.details,
        );
      case "201":
        return GoogleSignInNativeException(
          code: 201,
          message: "Login with Google cancelled",
          details: e.details,
        );
      case "202":
        if (e.details?.toString().contains('[28436]') == true) {
          return GoogleSignInNativeException(
            code: 205,
            message: "Temporarily blocked",
            details: e.details,
          );
        } else {
          return GoogleSignInNativeException(
            code: 202,
            message: "No Google credentials found",
            details: e.details,
          );
        }
      case "203":
        return GoogleSignInNativeException(
          code: 203,
          message: "Mismatched Google credentials",
          details: e.details,
        );
      case "204":
        return GoogleSignInNativeException(
          code: 204,
          message: "Login failed",
          details: e.details,
        );
      case "301":
        return GoogleSignInNativeException(
          code: 301,
          message: "Save Google Credentials cancelled",
          details: e.details,
        );
      case "501":
        return GoogleSignInNativeException(
          code: 501,
          message: "Received an invalid Google ID token response",
          details: e.details,
        );
      case "502":
        return GoogleSignInNativeException(
          code: 502,
          message: "Invalid request",
          details: e.details,
        );
      case "503":
        return GoogleSignInNativeException(
          code: 503,
          message: "Google client is not initialized yet",
          details: e.details,
        );
      case "505":
        return GoogleSignInNativeException(
          code: 505,
          message: "Google credential decode error",
          details: e.details,
        );
      default:
        return GoogleSignInNativeException(
          code: 504,
          message: e.message ?? "Credentials operation failed",
          details: e.details,
        );
    }
  }
}
