import 'package:flutter_test/flutter_test.dart';
import 'package:google_sign_in_native/google_sign_in_native.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockGoogleSignInNativePlatform
    with MockPlatformInterfaceMixin
    implements GoogleSignInNativePlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');

  @override
  Future<void> init(bool preferImmediatelyAvailableCredentials, String? googleClientId) {
    // TODO: implement init
    throw UnimplementedError();
  }

  @override
  Future<void> logout() {
    // TODO: implement logout
    throw UnimplementedError();
  }

  @override
  Future<GoogleIdTokenCredential?> googleSignIn(bool useButtonFlow, List<String>scopes) {
    // TODO: implement saveGoogleCredential
    throw UnimplementedError();
  }
}

void main() {
  final GoogleSignInNativePlatform initialPlatform = GoogleSignInNativePlatform.instance;

  test('$MethodChannelGoogleSignInNative is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelGoogleSignInNative>());
  });

  test('getPlatformVersion', () async {
    GoogleSignInNative googleSignInNativePlugin = GoogleSignInNative();
    MockGoogleSignInNativePlatform fakePlatform = MockGoogleSignInNativePlatform();
    GoogleSignInNativePlatform.instance = fakePlatform;

    expect(await googleSignInNativePlugin.getPlatformVersion(), '42');
  });

  test('google_sign_in', () async {

  });

  test('logout', () async {

  });
}
