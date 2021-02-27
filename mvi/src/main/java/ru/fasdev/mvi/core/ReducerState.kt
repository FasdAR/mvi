package ru.fasdev.mvi.core

import ru.fasdev.mvi.core.action.Action
import ru.fasdev.mvi.core.state.UiState

interface ReducerState <S : UiState> {
    fun reduce(action: Action, state: S): S? = null
}
