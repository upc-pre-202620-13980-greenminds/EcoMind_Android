package pe.greenminds.ecomind.iam.infrastructure.local

import kotlinx.coroutines.delay
import pe.greenminds.ecomind.iam.domain.model.AuthError
import pe.greenminds.ecomind.iam.domain.model.AuthException
import pe.greenminds.ecomind.iam.domain.model.PendingRegistration
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.iam.domain.model.SocialRole
import pe.greenminds.ecomind.iam.domain.repositories.AuthRepository
import pe.greenminds.ecomind.users.interfaces.acl.UsersContextFacade
import javax.inject.Inject
import javax.inject.Singleton

// Demo implementation used until the web services are deployed.
// It is a singleton because the registered accounts only live in memory.
@Singleton
class LocalAuthRepository @Inject constructor(
    private val usersContextFacade: UsersContextFacade
) : AuthRepository {

    private data class Account(
        val id: Long,
        val name: String,
        val email: String,
        val password: String,
        val role: SocialRole
    )

    private data class PendingAccount(
        val name: String,
        val email: String,
        val password: String,
        val role: SocialRole,
        val expiresAtMillis: Long
    )

    companion object {
        // Fictional account taken from the examples of the web services documentation
        private val DEMO_ACCOUNT = Account(
            id = 1L,
            name = "Camila Torres",
            email = "camila.torres@example.com",
            password = "GreenPlanet2026",
            role = SocialRole.STUDENT
        )

        // Second fictional account, made up for this app, to see the screens of a parent
        private val DEMO_PARENT_ACCOUNT = Account(
            id = 2L,
            name = "Robin Green",
            email = "robin.green@example.com",
            password = "GreenHome2026",
            role = SocialRole.PARENT
        )

        // Ids below this value belong to the sample users of the other contexts
        private const val FIRST_NEW_ACCOUNT_ID = 100L

        // No email is sent, so the code is the example of the web services documentation
        private const val DEMO_VERIFICATION_CODE = "482913"

        private const val DEMO_TOKEN = "demo-access-token"
        private const val SIMULATED_DELAY_MILLIS = 800L
        private const val SESSION_DURATION_MILLIS = 60 * 60 * 1000L
        private const val CODE_DURATION_MILLIS = 20 * 60 * 1000L
    }

    private val accounts = mutableListOf(DEMO_ACCOUNT, DEMO_PARENT_ACCOUNT)
    private var nextAccountId = FIRST_NEW_ACCOUNT_ID
    private var pendingAccount: PendingAccount? = null

    override suspend fun signIn(email: String, password: String): Result<Session> {
        // Simulates the time a request to the web services would take
        delay(SIMULATED_DELAY_MILLIS)

        // The answer is the same whether the email or the password is wrong
        val account = accounts.find { it.email == email && it.password == password }
            ?: return Result.failure(AuthException(AuthError.INVALID_CREDENTIALS))

        val session = Session(
            accountId = account.id,
            email = account.email,
            accessToken = DEMO_TOKEN,
            expiresAtMillis = System.currentTimeMillis() + SESSION_DURATION_MILLIS
        )
        return Result.success(session)
    }

    override suspend fun signUp(
        name: String,
        email: String,
        password: String,
        role: SocialRole
    ): Result<PendingRegistration> {
        delay(SIMULATED_DELAY_MILLIS)

        if (accounts.any { it.email == email }) {
            return Result.failure(AuthException(AuthError.EMAIL_CONFLICT))
        }

        // The account is created only after the email is verified
        val expiresAt = System.currentTimeMillis() + CODE_DURATION_MILLIS
        pendingAccount = PendingAccount(name, email, password, role, expiresAt)
        return Result.success(PendingRegistration(email, expiresAt))
    }

    override suspend fun verifyEmail(email: String, code: String): Result<Unit> {
        delay(SIMULATED_DELAY_MILLIS)

        // The answer is the same for a wrong code, an expired code or an unknown email
        val pending = pendingAccount
        if (pending == null ||
            pending.email != email ||
            System.currentTimeMillis() >= pending.expiresAtMillis ||
            code != DEMO_VERIFICATION_CODE
        ) {
            return Result.failure(AuthException(AuthError.VERIFICATION_CODE_INVALID))
        }

        val account = Account(
            id = nextAccountId++,
            name = pending.name,
            email = pending.email,
            password = pending.password,
            role = pending.role
        )
        accounts.add(account)
        pendingAccount = null

        // As in the web services, creating the account also creates its profile in Users
        usersContextFacade.createProfile(account.id, account.name, account.role.name)
        return Result.success(Unit)
    }
}
