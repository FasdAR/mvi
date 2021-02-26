package ru.fasdev.mvi.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import ru.fasdev.mvi.core.action.Action
import ru.fasdev.mvi.core.state.UiEffect
import ru.fasdev.mvi.core.state.UiState

class Store<S : UiState, E : UiEffect>(
    private val initState: S,
    private val reducerState: ReducerState<S>,
    private val middleware: Middleware<S>,
    private val reducerEffect: ReducerEffect<S, E>? = null
) {
    val state: MutableStateFlow<S> = MutableStateFlow(initState)
    val effect: MutableSharedFlow<E> = MutableSharedFlow()

    private val actionBus: MutableSharedFlow<Action> = MutableSharedFlow()

    val currentState: S = state.value

    @ExperimentalCoroutinesApi
    fun initBus(coroutineScope: CoroutineScope, initFlow: () -> Unit) {
        coroutineScope.launch {
            initAction()
        }

        coroutineScope.launch {
            initReduceState()
        }

        coroutineScope.launch {
            initReduceEffect()
        }

        coroutineScope.launch {
            actionBus
                .subscriptionCount
                .filter { it == 3 }
                .collect {
                    initFlow()
                }
        }
    }

    @ExperimentalCoroutinesApi
    @Suppress("UNCHECKED_CAST")
    private suspend fun initReduceState() {
        actionBus
            .scan(initState) { state, action ->
                reducerState.reduce(action, state) as S
            }
            .collect {
                if (currentState != it)
                    state.emit(it)
            }
    }

    private suspend fun initReduceEffect() {
        actionBus
            .filter { reducerEffect != null }
            .map { reducerEffect?.reduceEffect(it, currentState) }
            .collect {
                it?.let {
                    effect.emit(it)
                }
            }
    }

    @ExperimentalCoroutinesApi
    private suspend fun initAction() {
        merge(*middleware.bindMiddleware(actionBus, currentState).toTypedArray())
        .collect {
            actionBus.emit(it)
        }
    }

    suspend fun triggerAction(action: Action) {
        actionBus.emit(action)
    }
}
