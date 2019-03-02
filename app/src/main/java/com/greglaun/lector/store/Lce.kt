package com.greglaun.lector.store

sealed class Lce<out T> {

    open val data: T? = null

    abstract fun <R> map(f: (T) -> R): Lce<R>

    data class Success<out T>(override val data: T) : Lce<T>() {
        override fun <R> map(f: (T) -> R): Lce<R> = Success(f(data))
    }

    data class Error(val message: String) : Lce<Nothing>() {
        constructor(t: Throwable) : this(t.message ?: "")

        override fun <R> map(f: (Nothing) -> R): Lce<R> = this
    }

    object Loading : Lce<Nothing>() {
        override fun <R> map(f: (Nothing) -> R): Lce<R> = this
    }
}

val Lce<*>.debug: String
    get() =
        when (this) {
            is Lce.Success -> "S"
            is Lce.Loading -> "L"
            is Lce.Error -> "E"
        }