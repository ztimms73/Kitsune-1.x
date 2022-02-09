package org.xtimms.kitsune

import android.app.Application
import androidx.core.content.ContextCompat
import org.xtimms.kitsune.data.preference.PreferencesHelper
import uy.kohesive.injekt.api.*

class AppModule(val app: Application) : InjektModule {

    override fun InjektRegistrar.registerInjectables() {
        addSingleton(app)

        addSingletonFactory { PreferencesHelper(app) }

        ContextCompat.getMainExecutor(app).execute {
            get<PreferencesHelper>()
        }
    }
}