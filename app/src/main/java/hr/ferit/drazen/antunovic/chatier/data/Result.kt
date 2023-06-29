package hr.ferit.drazen.antunovic.chatier.data

sealed class Result<T>(val data: T? = null, val information: String? = null){
    class Waiting<T> : Result<T>()
    class Loading<T> : Result<T>()
    class Success<T>(data: T?): Result<T>(data = data)
    class Error<T>(information: String): Result<T>(information = information)
}
