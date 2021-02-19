package ru.fasdev.mvi.core

import ru.fasdev.mvi.core.action.Action
import ru.fasdev.mvi.core.state.State
import ru.fasdev.mvi.core.state.UiState

interface Reducer <S: UiState>
{
    fun reduce(action: Action, state: S): Set<State>
}