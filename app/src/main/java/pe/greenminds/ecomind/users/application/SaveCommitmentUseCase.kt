package pe.greenminds.ecomind.users.application

import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.users.domain.model.PersonalCommitment
import pe.greenminds.ecomind.users.domain.repositories.CommitmentRepository
import javax.inject.Inject

class SaveCommitmentUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val commitmentRepository: CommitmentRepository
) {

    suspend operator fun invoke(text: String): Result<Unit> {
        // 1 to 150 characters, without surrounding spaces
        val trimmed = text.trim()
        if (!PersonalCommitment.isValid(trimmed)) {
            return Result.failure(
                IllegalArgumentException(
                    "The commitment must have 1 to ${PersonalCommitment.MAX_LENGTH} characters"
                )
            )
        }

        val session = sessionRepository.getSession().first()
            ?: return Result.failure(IllegalStateException("There is no stored session"))

        // The user only writes their own commitment, so the id comes from the session
        return commitmentRepository.saveCommitment(session.accountId, trimmed)
    }
}
