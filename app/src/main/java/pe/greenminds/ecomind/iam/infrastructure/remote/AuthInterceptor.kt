package pe.greenminds.ecomind.iam.infrastructure.remote

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import pe.greenminds.ecomind.BuildConfig
import pe.greenminds.ecomind.iam.domain.model.Session
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
        // Retrofit tags stay inside the client. A captured session is a guard,
        // never a separate Authorization header or part of the HTTP payload.
        val capturedSession = request.tag(Session::class.java)
        if (capturedSession != null &&
            (capturedSession != session || capturedSession.isExpired(System.currentTimeMillis()))) {
            throw IOException("Session changed before the request could be sent")
        }
        if (session == null || session.isExpired(System.currentTimeMillis())) {
            return chain.proceed(request)
        }
        return chain.proceed(request.newBuilder()
            .header("Authorization", "Bearer ${session.accessToken}").build())
    }
}
