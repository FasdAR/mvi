package ru.fasdev.mvi.core

import ru.fasdev.mvi.core.action.Action
import ru.fasdev.mvi.core.state.State
import ru.fasdev.mvi.core.state.UiEffect
import ru.fasdev.mvi.core.state.UiState

interface ReducerEffect <S: UiState, E : UiEffect> {
    fun reduceEffect(action: Action, state: S): E? = null
}
