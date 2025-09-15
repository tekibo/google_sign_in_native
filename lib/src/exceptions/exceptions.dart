/// Exception class representing errors related to various authentication operations.
///
/// ```dart
///   code   message
///    101   Initialization failure
///    102   Plugin exception
///    103   Not implemented
///    201   Login with Google cancelled
///    202   No Google credentials found
///    203   Mismatched Google credentials
///    204   Login failed
///    205   Temporarily blocked
///    301   Save Google Credentials cancelled
///    501   Received an invalid Google ID token response
///    502   Invalid request
///    503   Google client is not initialized yet
///    504   Credentials operation failed
///    505   Google credential decode error
///    601   Activity not available
///    602   Authorization failed
/// ```
class GoogleSignInNativeException implements Exception {
  /// A numeric code identifying the specific error.
  final int code;

  /// A human-readable message describing the error.
  final String message;

  /// Additional details or context about the error.
  final String? details;

  /// Creates a [GoogleSignInNativeException] instance with the specified [code], [message], and [details].
  GoogleSignInNativeException({
    required this.code,
    required this.message,
    this.details,
  });

  /// Converts the exception to a JSON-compatible map.
  Map<String, dynamic> toJson() => {
    'code': code,
    'message': message,
    'details': details,
  };

  @override
  String toString() =>
      'GoogleSignInNativeException(code: $code, message: $message, details: $details)';
}