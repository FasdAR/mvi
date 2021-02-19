package ru.fasdev.mvi.core

import kotlinx.coroutines.flow.Flow
import ru.fasdev.mvi.core.action.Action
import ru.fasdev.mvi.core.state.UiState

interface Middleware<S : UiState> {
    fun bindMiddleware(actionBus: Flow<Action>, state: S): List<Flow<Action>>
}
