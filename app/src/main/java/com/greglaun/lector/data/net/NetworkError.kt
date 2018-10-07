package com.greglaun.lector.data.net

typealias HttpCode = Int

sealed class NetworkError
data class HttpError(val code : HttpCode) : NetworkError()
data class NetworkException(val throwable : Throwable) : NetworkError()