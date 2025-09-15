import 'package:google_sign_in_native/google_sign_in_native.dart';

/// Represents the result of an authorization request.
class GoogleAuthorizationResult {
  final String? accessToken;
  final String? serverAuthCode;
  final List<String>? grantedScopes;
  final GoogleSignInNativeException? error;

  GoogleAuthorizationResult({
    this.accessToken,
    this.serverAuthCode,
    this.grantedScopes,
    this.error,
  });

  factory GoogleAuthorizationResult.fromJson(Map<String, dynamic> json) {
    return GoogleAuthorizationResult(
      accessToken: json['accessToken'] as String?,
      serverAuthCode: json['serverAuthCode'] as String?,
      grantedScopes: (json['grantedScopes'] as List<dynamic>?)?.cast<String>(),
      error: json['error'] != null
          ? GoogleSignInNativeException(
        code: int.parse(json['error']['code'].toString()),
        message: json['error']['message'] as String,
        details: json['error']['details'],
      )
          : null,
    );
  }

  Map<String, dynamic> toJson() => {
    'accessToken': accessToken,
    'serverAuthCode': serverAuthCode,
    'grantedScopes': grantedScopes,
    'error': error?.toJson(),
  };
}