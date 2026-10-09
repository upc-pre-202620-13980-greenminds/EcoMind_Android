package pe.greenminds.ecomind.iam.infrastructure.remote

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import pe.greenminds.ecomind.BuildConfig
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val sessionRepository: SessionRepository
) : Interceptor {
    private val apiUrl = BuildConfig.API_BASE_URL.toHttpUrl()
    private val publicPaths = setOf("sign-in", "sign-up", "verify-email")
        .map { "${apiUrl.encodedPath}authentication/$it" }.toSet()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url
        if (url.scheme != apiUrl.scheme || url.host != apiUrl.host || url.port != apiUrl.port
            || !url.encodedPath.startsWith(apiUrl.encodedPath) || url.encodedPath in publicPaths
        ) {
            return chain.proceed(request)
        }

        val session = runBlocking { sessionRepository.getSession().first() }
        if (session == null || session.isExpired(System.currentTimeMillis())) {
            return chain.proceed(request)
        }
        return chain.proceed(request.newBuilder()
            .header("Authorization", "Bearer ${session.accessToken}").build())
    }
}

