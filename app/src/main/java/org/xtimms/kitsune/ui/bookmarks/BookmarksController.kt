package org.xtimms.kitsune.ui.bookmarks

import android.view.LayoutInflater
import android.view.View
import com.bluelinelabs.conductor.*
import com.bluelinelabs.conductor.viewpager.RouterPagerAdapter
import com.google.android.material.tabs.TabLayout
import org.xtimms.kitsune.R
import org.xtimms.kitsune.databinding.PagerControllerBinding
import org.xtimms.kitsune.ui.base.controller.RootController
import org.xtimms.kitsune.ui.base.controller.RxController
import org.xtimms.kitsune.ui.base.controller.TabbedController
import org.xtimms.kitsune.ui.main.MainActivity
import org.xtimms.kitsune.ui.stub.StubController

class BookmarksController :
    RxController<PagerControllerBinding>(),
    RootController,
    TabbedController {

    private var adapter: BookmarksAdapter? = null

    override fun getTitle(): String? {
        return resources!!.getString(R.string.stub)
    }

    override fun createBinding(inflater: LayoutInflater) = PagerControllerBinding.inflate(inflater)

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        adapter = BookmarksAdapter()
        binding.pager.adapter = adapter
    }

    override fun onDestroyView(view: View) {
        super.onDestroyView(view)
        adapter = null
    }

    override fun onChangeStarted(handler: ControllerChangeHandler, type: ControllerChangeType) {
        super.onChangeStarted(handler, type)
        if (type.isEnter) {
            (activity as? MainActivity)?.binding?.tabs?.apply {
                setupWithViewPager(binding.pager)
            }
        }
    }

    override fun configureTabs(tabs: TabLayout) {
        with(tabs) {
            tabGravity = TabLayout.GRAVITY_START
            tabMode = TabLayout.MODE_SCROLLABLE
        }
    }

    private inner class BookmarksAdapter : RouterPagerAdapter(this@BookmarksController) {

        private val tabTitles = listOf(
            R.string.stub,
            R.string.stub,
            R.string.stub
        )
            .map { resources!!.getString(it) }

        override fun getCount(): Int {
            return tabTitles.size
        }

        override fun configureRouter(router: Router, position: Int) {
            if (!router.hasRootController()) {
                val controller: Controller = when (position) {
                    TEST1_CONTROLLER -> StubController()
                    TEST2_CONTROLLER -> StubController()
                    TEST3_CONTROLLER -> StubController()
                    else -> error("Wrong position $position")
                }
                router.setRoot(RouterTransaction.with(controller))
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            return tabTitles[position]
        }
    }

    companion object {
        const val TEST1_CONTROLLER = 0
        const val TEST2_CONTROLLER = 1
        const val TEST3_CONTROLLER = 2
    }

}