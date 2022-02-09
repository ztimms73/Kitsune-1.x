package org.xtimms.kitsune.ui.main

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.Router
import com.google.android.material.navigation.NavigationBarView
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.flow.launchIn
import org.xtimms.kitsune.R
import org.xtimms.kitsune.data.preference.asImmediateFlow
import org.xtimms.kitsune.databinding.MainActivityBinding
import org.xtimms.kitsune.ui.base.activity.BaseViewBindingActivity
import org.xtimms.kitsune.ui.base.controller.RootController
import org.xtimms.kitsune.ui.base.controller.TabbedController
import org.xtimms.kitsune.ui.base.controller.withFadeTransaction
import org.xtimms.kitsune.ui.bookmarks.BookmarksController
import org.xtimms.kitsune.ui.stub.StubController
import org.xtimms.kitsune.util.system.dpToPx
import org.xtimms.kitsune.util.view.setNavigationBarTransparentCompat
import org.xtimms.kitsune.widget.search.MaterialSearchView
import org.xtimms.kitsune.widget.search.NavigationIconCompat

class MainActivity : BaseViewBindingActivity<MainActivityBinding>() {

    private lateinit var router: Router

    private val startScreenId by lazy {
        when (preferences.startScreen()) {
            2 -> R.id.nav_explore
            3 -> R.id.nav_bookmarks
            4 -> R.id.nav_updates
            else -> R.id.nav_shelf
        }
    }

    lateinit var tabAnimator: ViewHeightAnimator

    // To be checked by splash screen. If true then splash screen will be removed.
    var ready = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Prevent splash screen showing up on configuration changes
        val splashScreen = if (savedInstanceState == null) installSplashScreen() else null

        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)

        // Do not let the launcher create a new activity http://stackoverflow.com/questions/16283079
        if (!isTaskRoot) {
            finish()
            return
        }

        val toolbar = binding.materialSearchBar.getToolbar()

        setContentView(binding.root)
        setSupportActionBar(toolbar)

        binding.materialSearchBar.apply {
            navigationIconCompat = NavigationIconCompat.SEARCH
            setHint(getString(R.string.search))
            setOnClickListener {
                binding.materialSearchView.requestFocus()
            }
            setNavigationOnClickListener {
                binding.materialSearchView.requestFocus()
            }
        }

        val startTime = System.currentTimeMillis()
        splashScreen?.setKeepVisibleCondition {
            val elapsed = System.currentTimeMillis() - startTime
            elapsed <= SPLASH_MIN_DURATION || (!ready && elapsed <= SPLASH_MAX_DURATION)
        }
        setSplashScreenExitAnimation(splashScreen)

        binding.materialSearchView.apply {
            navigationIconCompat = NavigationIconCompat.ARROW
            setNavigationOnClickListener {
                binding.materialSearchView.clearFocus()
            }
            setHint(getString(R.string.search))
            setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: CharSequence) {

                }

                override fun onQueryTextSubmit(query: CharSequence) {

                }
            })
            setOnFocusChangeListener(object : MaterialSearchView.OnFocusChangeListener {
                override fun onFocusChange(hasFocus: Boolean) {

                }
            })
        }

        // Draw edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding.appbar.applyInsetter {
            type(navigationBars = true, statusBars = true) {
                padding(left = true, top = true, right = true)
            }
        }
        binding.materialSearchView.applyInsetter {
            type(navigationBars = true, statusBars = true) {
                padding(left = true, top = true, right = true)
            }
        }
        binding.bottomNavigation?.applyInsetter {
            type(navigationBars = true) {
                padding()
            }
        }

        tabAnimator = ViewHeightAnimator(binding.tabs)

        if (binding.sideNav != null) {
            preferences.sideNavIconAlignment()
                .asImmediateFlow {
                    binding.sideNav?.menuGravity = when (it) {
                        1 -> Gravity.CENTER
                        2 -> Gravity.BOTTOM
                        else -> Gravity.TOP
                    }
                }
                .launchIn(lifecycleScope)
        }

        nav.setOnItemSelectedListener { item ->
            val id = item.itemId

            val currentRoot = router.backstack.firstOrNull()
            if (currentRoot?.tag()?.toIntOrNull() != id) {
                when (id) {
                    R.id.nav_shelf -> setRoot(StubController(), id)
                    R.id.nav_explore -> setRoot(StubController(), id)
                    R.id.nav_bookmarks -> setRoot(BookmarksController(), id)
                    R.id.nav_updates -> setRoot(StubController(), id)
                }
            }
            true
        }

        val container: ViewGroup = binding.controllerContainer
        router = Conductor.attachRouter(this, container, savedInstanceState)
        if (!router.hasRootController()) {
            // Set start screen
            setSelectedNavItem(startScreenId)
        }

        router.addChangeListener(
            object : ControllerChangeHandler.ControllerChangeListener {
                override fun onChangeStarted(
                    to: Controller?,
                    from: Controller?,
                    isPush: Boolean,
                    container: ViewGroup,
                    handler: ControllerChangeHandler
                ) {
                    syncActivityViewWithController(to, from)
                }

                override fun onChangeCompleted(
                    to: Controller?,
                    from: Controller?,
                    isPush: Boolean,
                    container: ViewGroup,
                    handler: ControllerChangeHandler
                ) {
                }
            }
        )

        syncActivityViewWithController()
    }

    /**
     * Sets custom splash screen exit animation on devices prior to Android 12.
     *
     * When custom animation is used, status and navigation bar color will be set to transparent and will be restored
     * after the animation is finished.
     */
    private fun setSplashScreenExitAnimation(splashScreen: SplashScreen?) {
        val setNavbarScrim = {
            // Make sure navigation bar is on bottom before we modify it
            ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
                if (insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom > 0) {
                    val elevation = binding.bottomNavigation?.elevation ?: 0F
                    window.setNavigationBarTransparentCompat(this@MainActivity, elevation)
                }
                insets
            }
            ViewCompat.requestApplyInsets(binding.root)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && splashScreen != null) {
            val oldStatusColor = window.statusBarColor
            val oldNavigationColor = window.navigationBarColor
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT

            splashScreen.setOnExitAnimationListener { splashProvider ->
                // For some reason the SplashScreen applies (incorrect) Y translation to the iconView
                splashProvider.iconView.translationY = 0F

                val activityAnim = ValueAnimator.ofFloat(1F, 0F).apply {
                    interpolator = LinearOutSlowInInterpolator()
                    duration = SPLASH_EXIT_ANIM_DURATION
                    addUpdateListener { va ->
                        val value = va.animatedValue as Float
                        binding.root.translationY = value * 16.dpToPx
                    }
                }

                val splashAnim = ValueAnimator.ofFloat(1F, 0F).apply {
                    interpolator = FastOutSlowInInterpolator()
                    duration = SPLASH_EXIT_ANIM_DURATION
                    addUpdateListener { va ->
                        val value = va.animatedValue as Float
                        splashProvider.view.alpha = value
                    }
                    doOnEnd {
                        splashProvider.remove()
                        window.statusBarColor = oldStatusColor
                        window.navigationBarColor = oldNavigationColor
                        setNavbarScrim()
                    }
                }

                activityAnim.start()
                splashAnim.start()
            }
        } else {
            setNavbarScrim()
        }
    }

    override fun onResume() {
        super.onResume()
        syncActivityViewWithController()
    }

    @Suppress("UNNECESSARY_SAFE_CALL")
    override fun onDestroy() {
        super.onDestroy()

        // Binding sometimes isn't actually instantiated yet somehow
        nav?.setOnItemSelectedListener(null)
    }

    override fun onBackPressed() {
        binding.materialSearchView.clearFocus()
        val backstackSize = router.backstackSize
        if (backstackSize == 1 && router.getControllerWithTag("$startScreenId") == null) {
            // Return to start screen
            setSelectedNavItem(startScreenId)
        } else if (backstackSize == 1 || !router.handleBack()) {
            // Regular back
            super.onBackPressed()
        }
    }

    fun setSelectedNavItem(itemId: Int) {
        if (!isFinishing) {
            nav.selectedItemId = itemId
        }
    }

    private fun setRoot(controller: Controller, id: Int) {
        router.setRoot(controller.withFadeTransaction().tag(id.toString()))
    }

    private fun syncActivityViewWithController(
        to: Controller? = router.backstack.lastOrNull()?.controller,
        from: Controller? = null
    ) {

        supportActionBar?.setDisplayHomeAsUpEnabled(router.backstackSize != 1)

        // Always show appbar again when changing controllers
        binding.appbar.setExpanded(true)

        if ((from == null || from is RootController) && to !is RootController) {
            showNav(false)
        }
        if (to is RootController) {
            // Always show bottom nav again when returning to a RootController
            showNav(true)
        }

        if (from is TabbedController) {
            from.cleanupTabs(binding.tabs)
        }
        if (to is TabbedController) {
            tabAnimator.expand()
            to.configureTabs(binding.tabs)
        } else {
            tabAnimator.collapse()
            binding.tabs.setupWithViewPager(null)
        }
    }

    private fun showNav(visible: Boolean) {
        showBottomNav(visible)
        showSideNav(visible)
    }

    // Also used from some controllers to swap bottom nav with action toolbar
    fun showBottomNav(visible: Boolean) {
        if (visible) {
            //binding.bottomNavigation?.slideUp()
        } else {
            //binding.bottomNavigation?.slideDown()
        }
    }

    private fun showSideNav(visible: Boolean) {
        binding.sideNav?.isVisible = visible
    }

    private val nav: NavigationBarView
        get() = binding.bottomNavigation ?: binding.sideNav!!

    companion object {
        // Splash screen
        private const val SPLASH_MIN_DURATION = 500 // ms
        private const val SPLASH_MAX_DURATION = 5000 // ms
        private const val SPLASH_EXIT_ANIM_DURATION = 400L // ms
    }
}