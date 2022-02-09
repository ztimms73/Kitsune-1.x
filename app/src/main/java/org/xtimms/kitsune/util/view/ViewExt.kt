package org.xtimms.kitsune.util.view

import android.view.ViewGroup
import androidx.core.view.children

/**
 * Returns this ViewGroup's first child of specified class
 */
inline fun <reified T> ViewGroup.findChild(): T? {
    return children.find { it is T } as? T
}