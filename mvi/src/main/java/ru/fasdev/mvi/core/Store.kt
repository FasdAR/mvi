package ru.fasdev.mvi.core

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import ru.fasdev.mvi.BuildConfig
import ru.fasdev.mvi.core.action.Action
import ru.fasdev.mvi.core.state.UiEffect
import ru.fasdev.mvi.core.state.UiState

class Store<S : UiState, E : UiEffect>(
    initState: S,
    private val reducer: Reducer<S>,
    private val middleware: Middleware<S>
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
            initReduce()
        }

        coroutineScope.launch {
            actionBus
                .subscriptionCount
                .filter { it == 2 }
                .collect {
                    initFlow()
                }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun initReduce() {
        actionBus
            .map {
                val value = state.value
                val first = state.first()

                Log.d("STATES", "$value  |||  $first")

                val newState = reducer.reduce(it, currentState)

                if (BuildConfig.DEBUG) {
                    Log.d("SCAN_STATE", "Current State: $currentState, Action: $it, New State: $newState")
                }

                newState
            }
            .collect {
                it.forEach {
                    when (it) {
                        is UiState -> {
                            val newState = it as S
                            if (BuildConfig.DEBUG) {
                                Log.d("UI_STATE_COLLECT", "Current State: $currentState, New State: $newState")
                            }

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
