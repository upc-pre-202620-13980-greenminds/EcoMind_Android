package pe.greenminds.ecomind.learning.infrastructure

import kotlinx.coroutines.CancellationException
import pe.greenminds.ecomind.learning.domain.TutorialProgress
import retrofit2.Response
import javax.inject.Inject

class TutorialRepository @Inject constructor(private val api: LearningApi) {
    suspend fun progress() = execute { api.getTutorialProgress() }
    suspend fun next() = execute { api.completeTutorialStep() }
    suspend fun complete() = execute { api.completeTutorial() }

    private suspend fun execute(call: suspend () -> Response<TutorialProgressDto>): Result<TutorialProgress> = try {
        val response = call()
        if (response.isSuccessful) {
            val dto = requireNotNull(response.body())
            Result.success(TutorialProgress(dto.currentStep, dto.totalSteps, dto.completed))
        } else Result.failure(IllegalStateException("No se pudo guardar el avance del tutorial"))
    } catch (error: CancellationException) { throw error }
      catch (error: Exception) { Result.failure(error) }
}
