package ru.fasdev.mvi.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import ru.fasdev.mvi.core.action.Action
import ru.fasdev.mvi.core.state.UiEffect
import ru.fasdev.mvi.core.state.UiState

class Store<S: UiState, E: UiEffect>(initState: S, private val reducer: Reducer<S>,
    private val middleware: Middleware<S>)
{
    val state: MutableStateFlow<S> = MutableStateFlow(initState)
    val effect: MutableSharedFlow<E> = MutableSharedFlow()

    private val actionBus: MutableSharedFlow<Action> = MutableSharedFlow()

    val currentState: S = state.value

    fun initBus(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            initReduce()
        }

        coroutineScope.launch {
            initAction()
        }
    }

    private suspend fun initReduce() {
        actionBus
            .map { reducer.reduce(it, currentState) }
            .collect {
                it.forEach {
                    when (it) {
                        is UiState -> {
                            val newState = it as S
                            if (currentState != newState)
                                state.value = newState
                        }
                        is UiEffect -> {
                            effect.emit(it as E)
                        }
                    }
                }
            }
    }

    private suspend fun initAction() {
        merge(
            *middleware.bindMiddleware(actionBus, currentState).toTypedArray()
        )
        .collect(actionBus::emit)
    }

    suspend fun triggerAction(action: Action) {
        actionBus.emit(action)
    }
}