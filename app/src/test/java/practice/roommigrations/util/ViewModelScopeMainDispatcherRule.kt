@file:Suppress("DEPRECATION")

package practice.roommigrations.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.util.concurrent.Executors
import kotlin.coroutines.ContinuationInterceptor

@ExperimentalCoroutinesApi
class ViewModelScopeMainDispatcherRule(
    private val testContext: TestCoroutineContext?
) : TestWatcher() {

    private val singleThreadExecutor = Executors.newSingleThreadExecutor()

    override fun starting(description: Description?) {
        super.starting(description)
        if (testContext != null) {
            Dispatchers.setMain(testContext[ContinuationInterceptor] as CoroutineDispatcher)
        } else {
            Dispatchers.setMain(singleThreadExecutor.asCoroutineDispatcher())
        }
    }

    override fun finished(description: Description?) {
        super.finished(description)
        singleThreadExecutor.shutdownNow()
        Dispatchers.resetMain()
    }
}