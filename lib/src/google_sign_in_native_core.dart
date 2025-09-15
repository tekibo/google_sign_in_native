import 'package:google_sign_in_native/google_sign_in_native.dart';

class GoogleSignInNative {
  Future<String?> getPlatformVersion() {
    return GoogleSignInNativePlatform.instance.getPlatformVersion();
  }

  /// Initializes the Credential Manager.
  ///
  /// [preferImmediatelyAvailableCredentials] - Whether to prefer only locally-available credentials.
  /// [googleClientId] - The Google client ID to be used for Google credentials.
  ///
  /// Returns a [Future] that completes when initialization is successful.
  Future<void> init({
    required bool preferImmediatelyAvailableCredentials,
    String? googleClientId,
  }) async {
    return GoogleSignInNativePlatform.instance
        .init(preferImmediatelyAvailableCredentials, googleClientId);
  }

  /// Saves Google credentials.
  ///
  /// [useButtonFlow] - Whether to use the button flow for saving Google credentials.
  /// [scopes] - A list of scopes to request.
  ///
  /// Returns a [Future] that completes with [GoogleIdTokenCredential] representing the saved Google credentials.
  Future<GoogleIdTokenCredential?> googleSignIn(
      {bool useButtonFlow = false, List<String> scopes = const [],}) async {
    return GoogleSignInNativePlatform.instance
        .googleSignIn(useButtonFlow, scopes);
  }

  /// Logs out the user.
  ///
  /// Returns a [Future] that completes when the user is successfully logged out.
  Future<void> logout() async {
    return GoogleSignInNativePlatform.instance.logout();
  }

  /// Checks if the Credential Manager is supported on the current platform.
  ///
  /// Returns `true` if the platform is supported, otherwise `false`.
  bool get isSupportedPlatform => Platform.isAndroid || Platform.isIOS;
  
}
