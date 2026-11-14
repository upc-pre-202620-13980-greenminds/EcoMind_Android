package pe.greenminds.ecomind.users.domain.repositories

interface CommitmentRepository {

    // Null when the user has not written a commitment yet
    suspend fun getCommitment(userId: Long): Result<String?>

    // Adds the commitment or replaces the one the user already had
    suspend fun saveCommitment(userId: Long, text: String): Result<Unit>
}
