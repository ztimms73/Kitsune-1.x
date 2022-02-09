package org.xtimms.kitsune.ui.base.activity

import androidx.appcompat.app.AppCompatActivity
import org.xtimms.kitsune.data.preference.PreferencesHelper
import uy.kohesive.injekt.injectLazy

abstract class BaseThemedActivity : AppCompatActivity() {

    val preferences: PreferencesHelper by injectLazy()

}