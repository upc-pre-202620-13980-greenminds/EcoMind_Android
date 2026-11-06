package pe.greenminds.ecomind.iam.interfaces.navigation

import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object PasswordResetLink {
    const val BASE_URI = "ecomind://authentication/reset-password"

    fun matches(link: String?): Boolean {
        if (link == null) return false
        return runCatching {
            val uri = URI(link)
            uri.scheme == "ecomind" && uri.host == "authentication" && uri.path == "/reset-password"
        }.getOrDefault(false)
    }

    fun tokenFrom(link: String?): String? {
        if (link == null) return null
        return runCatching {
            val uri = URI(link)
            if (uri.scheme != "ecomind" || uri.host != "authentication" ||
                uri.path != "/reset-password" || uri.userInfo != null || uri.port != -1 ||
                uri.fragment != null
            ) return null

            val tokens = uri.rawQuery.orEmpty().split('&').mapNotNull { parameter ->
                val parts = parameter.split('=', limit = 2)
                if (parts.size == 2 && parts[0] == "token") {
                    URLDecoder.decode(parts[1], StandardCharsets.UTF_8.name())
                } else null
            }
            tokens.singleOrNull()?.takeIf { it.isNotBlank() && it.length <= 512 }
        }.getOrNull()
    }
}
