package pe.greenminds.ecomind.community.application.usecase

import javax.inject.Inject
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository

class UploadEventImageUseCase @Inject constructor(private val repository: CommunityRepository) {
    suspend operator fun invoke(localUri: String): Result<String> = repository.uploadEventImage(localUri)
}