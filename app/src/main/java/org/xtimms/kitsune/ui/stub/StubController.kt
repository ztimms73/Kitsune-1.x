package org.xtimms.kitsune.ui.stub

import android.view.LayoutInflater
import android.view.View
import org.xtimms.kitsune.R
import org.xtimms.kitsune.databinding.StubControllerBinding
import org.xtimms.kitsune.ui.base.controller.BaseController
import org.xtimms.kitsune.ui.base.controller.RootController

class StubController :
    BaseController<StubControllerBinding>(),
    RootController {

    override fun getTitle(): String? = "Stub"

    override fun createBinding(inflater: LayoutInflater) = StubControllerBinding.inflate(inflater)

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        binding.emptyView.show(R.drawable.ic_explore, "Work in progress")
    }

}