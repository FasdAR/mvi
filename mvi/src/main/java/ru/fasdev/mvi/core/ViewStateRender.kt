package ru.fasdev.mvi.core

import ru.fasdev.mvi.core.state.UiState

interface ViewStateRender<S : UiState> {
    fun render(state: S)
}
