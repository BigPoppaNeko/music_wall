package com.jfcardenas.musicwall.data

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val type: ErrorType, val message: String) : NetworkResult<Nothing>()
}

enum class ErrorType {
    NO_INTERNET,
    USER_NOT_FOUND,
    RATE_LIMIT,
    API_KEY_INVALID,
    EMPTY_RESPONSE,
    SERVER_ERROR,
    UNKNOWN
}
