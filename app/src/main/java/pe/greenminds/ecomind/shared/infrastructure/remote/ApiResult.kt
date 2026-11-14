package pe.greenminds.ecomind.shared.infrastructure.remote

import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import retrofit2.Response

internal suspend fun <T> apiResult(action: suspend () -> T): Result<T> = try {
    Result.success(action())
} catch (exception: CancellationException) {
    throw exception
} catch (exception: HttpException) {
    val error = try {
        exception.response()?.errorBody()?.use { Gson().fromJson(it.string(), ErrorDto::class.java) }
    } catch (_: Exception) { null }
    Result.failure(error?.toException() ?: exception)
} catch (exception: Exception) {
    Result.failure(exception)
}

internal fun <T : Any> Response<T>.requireBody(): T {
    if (!isSuccessful) throw HttpException(this)
    return body() ?: error("The backend returned an empty response")
}
