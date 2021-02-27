package ru.fasdev.mvi.core

import ru.fasdev.mvi.core.action.Action
import ru.fasdev.mvi.core.state.UiState

interface Middleware<S : UiState> {
    fun middleware(action: Action, state: S)
}
