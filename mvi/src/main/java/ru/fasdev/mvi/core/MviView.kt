package ru.fasdev.mvi.core

import ru.fasdev.mvi.core.state.UiEffect
import ru.fasdev.mvi.core.state.UiState

interface MviView<S : UiState, E : UiEffect> {
    fun render(state: S)
    fun renderEffect(effect: E)
}
