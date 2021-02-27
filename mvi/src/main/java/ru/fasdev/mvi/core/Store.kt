package ru.fasdev.mvi.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.fasdev.mvi.core.action.Action
import ru.fasdev.mvi.core.state.UiEffect
import ru.fasdev.mvi.core.state.UiState

class Store<S : UiState, E : UiEffect>(
    initState: S,
    private val reducerState: ReducerState<S>,
    private val middleware: Middleware<S>? = null,
    private val reducerEffect: ReducerEffect<S, E>? = null
) {
    val state: MutableStateFlow<S> = MutableStateFlow(initState)
    val effect: MutableSharedFlow<E> = MutableSharedFlow()

    val currentState: S
        get() = state.value

    fun procession (coroutineScope: CoroutineScope, action: Action) {
        coroutineScope.launch(Dispatchers.Default) {
            procession(action)
        }
    }

    suspend fun procession(action: Action) {
        var isActionApplied: Boolean = false

        reducerState.reduce(action, currentState)?.let {
            isActionApplied = true
            if (it != currentState)
                state.emit(it)
        }

        reducerEffect?.reduceEffect(action, currentState)?.let {
            isActionApplied = true
            effect.emit(it)
        }

        if (!isActionApplied)
            middleware?.middleware(action, currentState)
    }
}
