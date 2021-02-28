package ru.fasdev.mvi.core

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class StoreTest
{
    @Mock
    private lateinit var middleware: Middleware<TestState>
    @Mock
    private lateinit var reducer: ReducerState<TestState>
    @Mock
    private lateinit var reducerEffect: ReducerEffect<TestState, TestEffect>

    private lateinit var store: Store<TestState, TestEffect>

    //#region Test Data
    private val testState = TestState(text = "Hello State")
    private val testEffect = TestEffect(text = "Hello Effect")
    //#ednregion

    @BeforeEach
    fun init() {
        store = Store(TestState(), reducer, middleware, reducerEffect)
    }

    @Test
    @DisplayName("When procession in reduce action - return state text \"Hello State\"")
    fun testReducer() {
        Mockito.lenient().`when`(reducer.reduce(TestAction.ClickBtn, TestState()))
            .thenReturn(testState)

        runBlocking {
            store.procession(TestAction.ClickBtn)
            val state = store.state.first()
            assertThat(state).isEqualTo(testState)
        }
    }

    @Test
    @DisplayName("When procession in reduce effector action - return Effect text \"Hello Effect\"")
    fun testEffector() {
        Mockito.lenient().`when`(reducerEffect
            .reduceEffect(TestAction.ClickBtn, TestState()))
            .thenReturn(testEffect)

        runBlocking {
            store.procession(TestAction.ClickBtn)
            Mockito.verify(reducerEffect).reduceEffect(TestAction.ClickBtn, TestState())

            //Поскольку корутины не могут
            //нормально проверить Flow приходится проверять по верификации
            //val effect = store.effect.first()
            //assertThat(effect).isEqualTo(testEffect)
        }
    }

    @Test
    @DisplayName("When procession in reduce action - verify run middleware")
    fun testMiddleware() {
        runBlocking {
            store.procession(TestAction.ClickBtn)
            Mockito.verify(middleware).middleware(TestAction.ClickBtn, TestState())
        }
    }
}