package ru.fasdev.mvi.core

import ru.fasdev.mvi.core.state.UiEffect

interface ViewEffectRender<E : UiEffect> {
    fun renderEffect(effect: E)
}