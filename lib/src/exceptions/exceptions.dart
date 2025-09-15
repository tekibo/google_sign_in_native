/// Exception class representing errors related to various authentication operations.
///
/// ```dart
///   code   message
///    101   Initialization failure
///    102   Plugin exception
///    103   Not implemented
///
///    501   Received an invalid google id token response
///    502   Invalid request
///    503   Google client is not initialized yet
///    504   Credentials operation failed
///    505   Google credential decode error
/// ```
class GoogleSignInNativeException implements Exception {
  /// A numeric code identifying the specific error.
  final int code;

  /// A human-readable message describing the error.
  final String message;

  /// Additional details or context about the error.
  final dynamic details;

  /// Creates a [GoogleSignInNativeException] instance with the specified [code], [message], and [details].
  GoogleSignInNativeException({
    required this.code,
    required this.message,
    this.details, // Made optional to align with usage
  });

  /// Converts the exception to a JSON-compatible map.
  Map<String, dynamic> toJson() => {
    'code': code,
    'message': message,
    'details': details?.toString(), // Convert dynamic details to string
  };

  @override
  String toString() =>
      'GoogleSignInNativeException(code: $code, message: $message, details: $details)';
}