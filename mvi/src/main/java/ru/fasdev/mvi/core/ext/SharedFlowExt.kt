package ru.fasdev.mvi.core.ext

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.SharedFlow
import ru.fasdev.mvi.core.state.State

inline fun <reified T : State>SharedFlow<T>.registerRender(owner: LifecycleOwner, crossinline render: (T) -> Unit) {
    asLiveData().observe(owner) {
        render(it)
    }
}
