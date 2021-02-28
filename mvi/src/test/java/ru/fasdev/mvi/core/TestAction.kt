package ru.fasdev.mvi.core

import ru.fasdev.mvi.core.action.UiAction

sealed class TestAction: UiAction
{
    object ClickBtn: TestAction()
}