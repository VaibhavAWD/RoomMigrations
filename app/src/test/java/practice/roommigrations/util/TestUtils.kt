package practice.roommigrations.util

import androidx.lifecycle.LiveData
import com.google.common.truth.Truth.assertThat

fun <T : Any> assertLiveDataEventTriggered(liveData: LiveData<Event<T>>, expectedValue: T) {
    val event = LiveDataTestUtil.getValue(liveData)
    val content = event.getContentIfNotHandled()
    assertThat(content).isEqualTo(expectedValue)
}