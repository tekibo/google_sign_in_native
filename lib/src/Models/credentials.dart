import 'package:google_sign_in_native/src/Models/google_user_model.dart';

/// Represents a set of various types of credentials.
class Credentials {
  /// The Google ID token credential.
  final GoogleIdTokenCredential? googleIdTokenCredential;

  /// Constructs a new [Credentials] instance.
  /// [googleIdTokenCredential] is the Google ID token credential.
  Credentials({
    this.googleIdTokenCredential,
  });

  /// Creates a copy of this [Credentials] instance with the specified fields replaced.
  /// [googleIdTokenCredential] (optional) is the new Google ID token credential.
  Credentials copyWith({
    GoogleIdTokenCredential? googleIdTokenCredential,
  }) {
    return Credentials(
      googleIdTokenCredential:
      googleIdTokenCredential ?? this.googleIdTokenCredential,
    );
  }
}