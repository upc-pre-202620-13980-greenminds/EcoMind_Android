package pe.greenminds.ecomind.monetization.infrastructure.implementation

import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import pe.greenminds.ecomind.monetization.domain.model.Cosmetic
import pe.greenminds.ecomind.monetization.domain.model.GemPackage
import pe.greenminds.ecomind.monetization.domain.model.Multiplier
import pe.greenminds.ecomind.monetization.domain.model.StreakProtector
import pe.greenminds.ecomind.monetization.domain.model.UserCosmetic
import pe.greenminds.ecomind.monetization.domain.repositories.StoreRepository
import pe.greenminds.ecomind.monetization.infrastructure.local.LocalStoreRepository
import pe.greenminds.ecomind.monetization.infrastructure.remote.BuyItemDto
import pe.greenminds.ecomind.monetization.infrastructure.remote.MonetizationApi
import pe.greenminds.ecomind.monetization.infrastructure.remote.toDomain
import pe.greenminds.ecomind.shared.infrastructure.remote.ErrorDto
import pe.greenminds.ecomind.shared.infrastructure.remote.toException
import retrofit2.HttpException
import retrofit2.Response
import java.util.UUID
import javax.inject.Inject

class RemoteStoreRepository @Inject constructor(
    private val api: MonetizationApi,
    private val localCatalog: LocalStoreRepository
) : StoreRepository {

    override suspend fun getCosmetics(): Result<List<Cosmetic>> = execute(
        request = { api.getCosmetics() },
        transform = { cosmetics -> cosmetics.map { it.toDomain() } }
    )

    override suspend fun getUserCosmetics(): Result<List<UserCosmetic>> = execute(
        request = { api.getInventory() },
        transform = { it.cosmetics.map { cosmetic -> cosmetic.toDomain() } }
    )

    override suspend fun purchaseCosmetic(cosmeticId: String): Result<Unit> = execute(
        request = {
            api.purchaseCosmetic(
                BuyItemDto(itemId = cosmeticId, requestId = UUID.randomUUID().toString())
            )
        },
        transform = { _ -> kotlin.Unit }
    )

    override suspend fun setCosmeticEquipped(
        cosmeticId: String,
        equipped: Boolean
    ): Result<Unit> = executeEmpty {
        if (equipped) api.equipCosmetic(cosmeticId) else api.unequipCosmetic(cosmeticId)
    }

    override suspend fun getGemBalance(): Result<Int> = execute(
        request = { api.getWallet() },
        transform = { it.balance }
    )

    // These tabs will be migrated in the next steps. Keeping their current data avoids
    // disabling already completed UI while cosmetics starts using the backend.
    override suspend fun getMultipliers(): Result<List<Multiplier>> =
        localCatalog.getMultipliers()

    override suspend fun getStreakProtectors(): Result<List<StreakProtector>> =
        localCatalog.getStreakProtectors()

    override suspend fun getGemPackages(): Result<List<GemPackage>> =
        localCatalog.getGemPackages()

    private suspend fun <Dto : Any, Model> execute(
        request: suspend () -> Response<Dto>,
        transform: (Dto) -> Model
    ): Result<Model> = try {
        val response = request()
        if (!response.isSuccessful) {
            Result.failure(response.toException())
        } else {
            val body = response.body()
                ?: throw IllegalStateException("Empty monetization response")
            Result.success(transform(body))
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }

    private suspend fun executeEmpty(
        request: suspend () -> Response<Unit>
    ): Result<Unit> = try {
        val response = request()
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(response.toException())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }

    private fun Response<*>.toException(): Throwable {
        val error = try {
            errorBody()?.use { Gson().fromJson(it.string(), ErrorDto::class.java) }
        } catch (_: Exception) {
            null
        }
        return error?.toException() ?: HttpException(this)
    }
}
