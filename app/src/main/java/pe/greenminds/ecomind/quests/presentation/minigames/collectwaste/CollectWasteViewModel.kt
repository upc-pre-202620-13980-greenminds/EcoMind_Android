package pe.greenminds.ecomind.quests.presentation.minigames.collectwaste

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.quests.infrastructure.implementation.MinigameReward
import pe.greenminds.ecomind.quests.infrastructure.implementation.RemoteMinigameAttempts
import pe.greenminds.ecomind.quests.infrastructure.remote.MinigameAttemptDto
import pe.greenminds.ecomind.quests.interfaces.navigation.MinigameRoute
import pe.greenminds.ecomind.quests.presentation.minigames.NativeMinigame
import retrofit2.HttpException

private const val ROUND_SECONDS = 20
private const val WASTE_CELLS = 9
private const val WASTE_KINDS = 3
private const val TARGET_MILLIS = 1_400L

internal data class WasteTarget(val sequence: Int, val cell: Int, val kind: Int) {
    fun next() = WasteTarget(
        sequence + 1,
        (cell + Random.nextInt(1, WASTE_CELLS)) % WASTE_CELLS,
        Random.nextInt(WASTE_KINDS)
    )
}

internal sealed interface CollectWasteState {
    data object Loading : CollectWasteState
    data object Closed : CollectWasteState
    data object Unsupported : CollectWasteState
    data class Playing(
        val target: WasteTarget,
        val seconds: Int,
        val score: Int,
        val minScore: Int
    ) : CollectWasteState
    data class Result(
        val score: Int,
        val successful: Boolean,
        val reward: MinigameReward?
    ) : CollectWasteState
    data class Error(val action: CollectWasteAction, val hasOtherActiveAttempt: Boolean = false) : CollectWasteState
}

internal enum class CollectWasteAction { START, FINISH, CANCEL }

@HiltViewModel
internal class CollectWasteViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val attempts: RemoteMinigameAttempts
) : ViewModel() {
    private val route = savedState.toRoute<MinigameRoute>()
    private val mutableState = MutableStateFlow<CollectWasteState>(CollectWasteState.Loading)
    val state = mutableState.asStateFlow()

    private var timer: Job? = null
    private var deadline = 0L
    private var targetDeadline = 0L
    private var attemptId: Long? = savedState["finishedAttemptId"]
    private var finishedScore: Int? = savedState["finishedScore"]
    private var completedAttempt: MinigameAttemptDto? = null

    init {
        if (attemptId != null && finishedScore != null) submitResult() else start()
    }

    private fun start() = runAction(CollectWasteAction.START) {
        val game = attempts.getMinigame(route.minigameId)
        if (NativeMinigame.fromUrl(game.url) == NativeMinigame.COLLECT_WASTE) {
            startCollectWaste(requireNotNull(game.completionRules.minScore))
        } else {
            mutableState.value = CollectWasteState.Unsupported
        }
    }

    private suspend fun startCollectWaste(minScore: Int) {
        attemptId = attempts.start(route.questId, route.minigameId)
        val now = SystemClock.elapsedRealtime()
        deadline = now + ROUND_SECONDS * 1_000L
        targetDeadline = now + TARGET_MILLIS
        mutableState.value = CollectWasteState.Playing(
            WasteTarget(0, Random.nextInt(WASTE_CELLS), Random.nextInt(WASTE_KINDS)),
            ROUND_SECONDS,
            score = 0,
            minScore = minScore
        )
        timer = viewModelScope.launch {
            while (true) {
                val playing = mutableState.value as? CollectWasteState.Playing ?: break
                val time = SystemClock.elapsedRealtime()
                val remaining = deadline - time
                if (remaining <= 0) {
                    finish(playing.score)
                    break
                }
                val target = if (time >= targetDeadline) nextTarget(playing.target, time) else playing.target
                mutableState.value = playing.copy(target = target, seconds = ((remaining + 999) / 1_000).toInt())
                delay(minOf(remaining, 100))
            }
        }
    }

    fun collect(target: WasteTarget) {
        val playing = mutableState.value as? CollectWasteState.Playing ?: return
        val now = SystemClock.elapsedRealtime()
        if (playing.target != target || now >= deadline || now >= targetDeadline) return
        mutableState.value = playing.copy(target = nextTarget(target, now), score = playing.score + 1)
    }

    private fun nextTarget(target: WasteTarget, now: Long): WasteTarget {
        targetDeadline = now + TARGET_MILLIS
        return target.next()
    }

    private fun finish(score: Int) {
        finishedScore = score
        savedState["finishedAttemptId"] = attemptId
        savedState["finishedScore"] = score
        submitResult()
    }

    private fun submitResult() = runAction(CollectWasteAction.FINISH) {
        val attempt = completedAttempt ?: attempts.finish(
            requireNotNull(attemptId), route.minigameId, requireNotNull(finishedScore)
        ).also { completedAttempt = it }
        val successful = requireNotNull(attempt.successful)
        val reward = try {
            if (successful) attempts.rewardFor(attempt) else null
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            null
        }
        mutableState.value = CollectWasteState.Result(
            requireNotNull(attempt.score), successful, reward
        )
    }

    fun replay() {
        if (mutableState.value !is CollectWasteState.Result) return
        completedAttempt = null
        attemptId = null
        finishedScore = null
        savedState.remove<Long>("finishedAttemptId")
        savedState.remove<Int>("finishedScore")
        start()
    }

    fun retryReward() {
        val result = mutableState.value as? CollectWasteState.Result ?: return
        if (result.successful && result.reward == null) submitResult()
    }

    fun retry() {
        val error = mutableState.value as? CollectWasteState.Error ?: return
        when (error.action) {
            CollectWasteAction.START -> start()
            CollectWasteAction.FINISH -> submitResult()
            CollectWasteAction.CANCEL -> cancel()
        }
    }

    fun back() {
        when (mutableState.value) {
            CollectWasteState.Loading, CollectWasteState.Closed -> Unit
            is CollectWasteState.Playing -> cancel()
            else -> mutableState.value = CollectWasteState.Closed
        }
    }

    private fun cancel() {
        timer?.cancel()
        runAction(CollectWasteAction.CANCEL) {
            attempts.cancel(requireNotNull(attemptId), route.minigameId)
            mutableState.value = CollectWasteState.Closed
        }
    }

    private fun runAction(action: CollectWasteAction, block: suspend () -> Unit) {
        mutableState.value = CollectWasteState.Loading
        viewModelScope.launch {
            try {
                block()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                val conflict = action == CollectWasteAction.START && (error as? HttpException)?.code() == 409
                mutableState.value = CollectWasteState.Error(action, conflict)
            }
        }
    }
}
