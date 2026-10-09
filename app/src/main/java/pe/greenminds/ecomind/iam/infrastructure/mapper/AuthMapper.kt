package pe.greenminds.ecomind.iam.infrastructure.mapper

import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.iam.domain.model.PendingRegistration
import pe.greenminds.ecomind.iam.infrastructure.remote.SignInResponse
import pe.greenminds.ecomind.iam.infrastructure.remote.SignUpResponse
import java.time.Instant

fun SignInResponse.toDomain(): Session {
    return Session(
        accountId = accountId,
        email = email,
        accessToken = accessToken,
        expiresAtMillis = Instant.parse(expiresAt).toEpochMilli()
    )
}

fun SignUpResponse.toDomain(): PendingRegistration {
    return PendingRegistration(
        email = email,
        expiresAtMillis = Instant.parse(expiresAt).toEpochMilli()
    )
}
