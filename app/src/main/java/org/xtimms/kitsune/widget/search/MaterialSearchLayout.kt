package org.xtimms.kitsune.widget.search

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.Nullable
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import org.xtimms.kitsune.R

abstract class MaterialSearchLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    @NavigationIconCompat
    @get:NavigationIconCompat
    var navigationIconCompat: Int = 0
        set(@NavigationIconCompat navigationIconCompat) {
            field = navigationIconCompat

            when (navigationIconCompat) {
                NavigationIconCompat.NONE -> {
                    setNavigationIcon(0)
                }
                NavigationIconCompat.ARROW -> {
                    setNavigationIcon(
                            ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_arrow_back
                            )
                    )
                }
                NavigationIconCompat.SEARCH -> {
                    setNavigationIcon(
                            ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_search
                            )
                    )
                }
            }
        }

    abstract fun setNavigationIcon(@DrawableRes resId: Int)

    abstract fun setNavigationIcon(@Nullable drawable: Drawable?)

    abstract fun setNavigationContentDescription(@StringRes resId: Int)

    abstract fun setNavigationContentDescription(@Nullable description: CharSequence?)

    abstract fun setNavigationOnClickListener(listener: OnClickListener)

    abstract fun setNavigationElevation(elevation: Float)

    abstract fun setNavigationBackgroundColor(@ColorInt color: Int)

}