package pe.greenminds.ecomind.community.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.community.application.usecase.CancelEventGroupUseCase
import pe.greenminds.ecomind.community.application.usecase.GetEventChatMessagesUseCase
import pe.greenminds.ecomind.community.application.usecase.GetEventParticipationUseCase
import pe.greenminds.ecomind.community.application.usecase.LeaveEventUseCase
import pe.greenminds.ecomind.community.application.usecase.RegisterForEventUseCase
import pe.greenminds.ecomind.community.application.usecase.SendEventChatMessageUseCase
import pe.greenminds.ecomind.community.domain.entity.EventParticipation
import pe.greenminds.ecomind.community.domain.valueobjects.EventChatUnavailableException
import pe.greenminds.ecomind.community.presentation.states.CommunityEventsUiState
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.community.application.usecase.DeleteCommunityEventUseCase
@HiltViewModel
class CommunityEventsViewModel @Inject constructor(
    private val getParticipation: GetEventParticipationUseCase,
    private val registerForEvent: RegisterForEventUseCase,
    private val leaveEvent: LeaveEventUseCase,
    private val deleteEvent: DeleteCommunityEventUseCase,
    private val cancelGroup: CancelEventGroupUseCase,
    private val getMessages: GetEventChatMessagesUseCase,
    private val sendMessage: SendEventChatMessageUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(CommunityEventsUiState())
    val state = _state.asStateFlow()

    private var loadJob: Job? = null
    private var sendJob: Job? = null
    private var cursor = 0L
    private var chatVersion = 0L
    private var pendingClientId: String? = null

    fun bindCommunity(communityId: Long?) {
        if (_state.value.communityId == communityId) return

        loadJob?.cancel()
        closeChat()

        _state.update {
            it.copy(
                communityId = communityId,
                mine = false,
                items = emptyList(),
                search = "",
                error = null,
                errorRes = null,
                joinTarget = null,
                leaveTarget = null,
                joinedAsFamily = null
            )
        }

        refresh()
    }

    fun refresh() {
        loadJob?.cancel()
        val snapshot = _state.value

        if (!snapshot.mine && snapshot.communityId == null) {
            _state.update { it.copy(loading = false, items = emptyList(), error = null, errorRes = null) }
            return
        }

        loadJob = viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, errorRes = null) }

            getParticipation(
                if (snapshot.mine) null else snapshot.communityId
            ).fold(
                onSuccess = { items ->
                    _state.update { it.copy(loading = false, items = items) }
                },
                onFailure = { exception ->
                    _state.update {
                        it.copy(
                            loading = false,
                            error = exception.message?.takeIf { message -> message.isNotBlank() },
                            errorRes = R.string.event_flow_load_error
                        )
                    }
                }
            )
        }
    }

    fun toggleMine() {
        _state.update { it.copy(mine = !it.mine, search = "", items = emptyList()) }
        refresh()
    }

    fun showMine() {
        _state.update { it.copy(mine = true, search = "", items = emptyList()) }
        refresh()
    }

    fun showCommunityEvents() {
        _state.update { it.copy(mine = false, search = "", items = emptyList()) }
        refresh()
    }

    fun search(value: String) {
        _state.update { it.copy(search = value) }
    }

    fun openJoin(item: EventParticipation) {
        if (_state.value.busy || item.joined) return
        _state.update { it.copy(joinTarget = item, error = null, errorRes = null) }
    }

    fun dismissJoin() {
        if (!_state.value.busy) {
            _state.update { it.copy(joinTarget = null) }
        }
    }

    fun join(asFamily: Boolean) {
        val item = _state.value.joinTarget ?: return
        if (_state.value.busy) return
        if (asFamily && item.familyId == null) return

        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null, errorRes = null) }

            registerForEvent(
                item.event.id,
                if (asFamily) item.familyId else null
            ).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            busy = false,
                            joinTarget = null,
                            joinedAsFamily = asFamily
                        )
                    }
                    refresh()
                },
                onFailure = { exception ->
                    _state.update {
                        it.copy(
                            busy = false,
                            error = exception.message?.takeIf { message -> message.isNotBlank() },
                            errorRes = R.string.event_flow_join_error
                        )
                    }
                }
            )
        }
    }

    fun dismissJoined() {
        _state.update { it.copy(joinedAsFamily = null) }
    }

    fun requestLeave(item: EventParticipation) {
        if (_state.value.busy) return
        if (!item.creator && !item.joined && !item.canCancelGroup) return

        _state.update {
            it.copy(
                leaveTarget = item,
                error = null,
                errorRes = null
            )
        }
    }

    fun dismissLeave() {
        if (!_state.value.busy) {
            _state.update { it.copy(leaveTarget = null) }
        }
    }

    fun leave(wholeGroup: Boolean) {
        val item = _state.value.leaveTarget ?: return
        if (_state.value.busy) return

        if (!item.creator) {
            if (wholeGroup && (!item.canCancelGroup || item.registrationId == null)) return
            if (!wholeGroup && !item.joined) return
        }

        loadJob?.cancel()
        _state.update {
            it.copy(
                busy = true,
                loading = false,
                error = null,
                errorRes = null
            )
        }

        viewModelScope.launch {
            val result = when {
                item.creator -> deleteEvent(item.event.id)
                wholeGroup -> cancelGroup(
                    item.event.id,
                    checkNotNull(item.registrationId)
                )
                else -> leaveEvent(item.event.id)
            }

            result.fold(
                onSuccess = {
                    if (_state.value.chat?.event?.id == item.event.id) {
                        closeChat()
                    }

                    _state.update {
                        it.copy(
                            busy = false,
                            leaveTarget = null,
                            items = it.items.filterNot { participation ->
                                participation.event.id == item.event.id
                            }
                        )
                    }

                    refresh()
                },
                onFailure = { exception ->
                    _state.update {
                        it.copy(
                            busy = false,
                            error = exception.message?.takeIf { message ->
                                message.isNotBlank()
                            },
                            errorRes = if (item.creator) {
                                R.string.event_flow_delete_error
                            } else {
                                R.string.event_flow_leave_error
                            }
                        )
                    }
                }
            )
        }
    }

    fun openChat(item: EventParticipation) {
        if (!item.chatAvailable) return
        sendJob?.cancel()
        chatVersion++
        cursor = 0L
        pendingClientId = null

        _state.update {
            it.copy(
                chat = item,
                messages = emptyList(),
                chatAuthorId = null,
                chatLoading = true,
                chatClosed = false,
                chatError = null,
                chatErrorRes = null,
                draft = "",
                sending = false
            )
        }
    }

    fun closeChat() {
        sendJob?.cancel()
        chatVersion++
        cursor = 0L
        pendingClientId = null

        _state.update {
            it.copy(
                chat = null,
                messages = emptyList(),
                chatAuthorId = null,
                chatLoading = false,
                chatClosed = false,
                chatError = null,
                chatErrorRes = null,
                draft = "",
                sending = false
            )
        }
    }

    fun changeDraft(value: String) {
        if (_state.value.sending) return
        if (value != _state.value.draft) pendingClientId = null
        _state.update { it.copy(draft = value.take(2000)) }
    }

    suspend fun pollChat() {
        val eventId = _state.value.chat?.event?.id ?: return
        val version = chatVersion

        while (
            kotlinx.coroutines.currentCoroutineContext().isActive &&
            version == chatVersion &&
            !_state.value.chatClosed
        ) {
            val result = getMessages(eventId, cursor)

            if (version != chatVersion || _state.value.chatClosed) {
                return
            }

            var fullPage = false

            result.fold(
                onSuccess = { page ->
                    val lastId = page.messages.maxOfOrNull { it.id }

                    if (lastId != null) {
                        cursor = maxOf(cursor, lastId)
                    }

                    fullPage = page.messages.size == 50

                    _state.update {
                        it.copy(
                            chatLoading = false,
                            chatError = null,
                            chatErrorRes = null,
                            chatAuthorId = page.authorId,
                            messages = (it.messages + page.messages)
                                .distinctBy { message -> message.id }
                                .sortedBy { message -> message.id }
                        )
                    }
                },
                onFailure = { exception ->
                    val closed = exception is EventChatUnavailableException

                    _state.update {
                        it.copy(
                            chatLoading = false,
                            chatClosed = it.chatClosed || closed,
                            sending = if (closed) false else it.sending,
                            messages = if (closed) emptyList() else it.messages,
                            chatError = if (closed) {
                                null
                            } else {
                                exception.message?.takeIf { message ->
                                    message.isNotBlank()
                                }
                            },
                            chatErrorRes = if (closed) {
                                null
                            } else {
                                R.string.event_flow_messages_error
                            }
                        )
                    }
                }
            )

            if (_state.value.chatClosed) {
                return
            }

            if (fullPage) delay(50) else delay(3000)
        }
    }

    fun send() {
        val snapshot = _state.value
        val eventId = snapshot.chat?.event?.id ?: return
        val text = snapshot.draft.trim()

        if (text.isBlank() || snapshot.sending || snapshot.chatClosed) {
            return
        }

        val version = chatVersion
        val clientId = pendingClientId ?: UUID.randomUUID().toString().also {
            pendingClientId = it
        }

        sendJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    sending = true,
                    chatError = null,
                    chatErrorRes = null
                )
            }

            val result = sendMessage(eventId, text, clientId)

            if (version != chatVersion) {
                return@launch
            }

            if (_state.value.chatClosed) {
                _state.update { it.copy(sending = false) }
                return@launch
            }

            result.fold(
                onSuccess = { message ->
                    pendingClientId = null

                    _state.update {
                        it.copy(
                            sending = false,
                            draft = "",
                            messages = (it.messages + message)
                                .distinctBy { existing -> existing.id }
                                .sortedBy { existing -> existing.id }
                        )
                    }
                },
                onFailure = { exception ->
                    val closed = exception is EventChatUnavailableException

                    _state.update {
                        it.copy(
                            sending = false,
                            chatClosed = it.chatClosed || closed,
                            messages = if (closed) emptyList() else it.messages,
                            chatError = if (closed) {
                                null
                            } else {
                                exception.message?.takeIf { message ->
                                    message.isNotBlank()
                                }
                            },
                            chatErrorRes = if (closed) {
                                null
                            } else {
                                R.string.event_flow_send_error
                            }
                        )
                    }
                }
            )
        }
    }
}
