package org.xtimms.kitsune.data.preference

import android.content.Context
import androidx.preference.PreferenceManager
import com.tfcporciuncula.flow.FlowSharedPreferences
import com.tfcporciuncula.flow.Preference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import org.xtimms.kitsune.data.preference.PreferenceKeys as Keys

fun <T> Preference<T>.asImmediateFlow(block: (T) -> Unit): Flow<T> {
    block(get())
    return asFlow()
        .onEach { block(it) }
}

class PreferencesHelper(val context: Context) {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val flowPrefs = FlowSharedPreferences(prefs)

    fun confirmExit() = prefs.getBoolean(Keys.confirmExit, false)

    fun startScreen() = prefs.getInt(Keys.startScreen, 1)

    fun hideBottomBarOnScroll() = flowPrefs.getBoolean(Keys.hideBottomBarOnScroll, true)

    fun sideNavIconAlignment() = flowPrefs.getInt(Keys.sideNavIconAlignment, 0)

}