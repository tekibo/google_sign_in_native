import 'package:google_sign_in_native/google_sign_in_native.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

abstract class GoogleSignInNativePlatform extends PlatformInterface {
  /// Constructs a GoogleSignInNativePlatform.
  GoogleSignInNativePlatform() : super(token: _token);

  static final Object _token = Object();

  static GoogleSignInNativePlatform _instance =
      MethodChannelGoogleSignInNative();

  /// The default instance of [GoogleSignInNativePlatform] to use.
  ///
  /// Defaults to [MethodChannelGoogleSignInNative].
  static GoogleSignInNativePlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [GoogleSignInNativePlatform] when
  /// they register themselves.
  static set instance(GoogleSignInNativePlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  /// Initializes the credential manager with the option to prefer immediately available credentials.
  Future<void> init(
    bool preferImmediatelyAvailableCredentials,
    String? googleClientId,
  ) {
    return _instance.init(
      preferImmediatelyAvailableCredentials,
      googleClientId,
    );
  }

  Future<GoogleIdTokenCredential?> googleSignIn(
    bool useButtonFlow,
    List<String> scopes,
  ) async {
    return _instance.googleSignIn(useButtonFlow, scopes);
  }

  //Logout
  Future<void> logout() async {
    return _instance.logout();
  }
}
