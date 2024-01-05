package app.fyreplace.fyreplace.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.FragmentTextInputBinding
import app.fyreplace.fyreplace.extensions.hideSoftInput
import app.fyreplace.fyreplace.extensions.mainActivity
import app.fyreplace.fyreplace.extensions.showSoftInput
import app.fyreplace.fyreplace.viewmodels.TextInputViewModel
import kotlinx.coroutines.flow.combine

abstract class TextInputFragment : BaseFragment(R.layout.fragment_text_input), MenuProvider {
    override val rootView get() = if (::bd.isInitialized) bd.root else null
    abstract override val vm: TextInputViewModel
    abstract val maxLength: Int
    abstract val allowEmpty: Boolean
    protected lateinit var bd: FragmentTextInputBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = super.onCreateView(inflater, container, savedInstanceState)?.also {
        bd = FragmentTextInputBinding.bind(it).apply {
            lifecycleOwner = viewLifecycleOwner
            vm = this@TextInputFragment.vm
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.text.launchCollect(viewLifecycleOwner.lifecycleScope) {
            mainActivity.setToolbarInfo(getString(R.string.text_input_length, it.length, maxLength))
        }
    }

    override fun onStart() {
        super.onStart()
        bd.text.showSoftInput()
    }

    override fun onStop() {
        bd.text.hideSoftInput()
        super.onStop()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.fragment_text_input, menu)
        vm.isLoading
            .combine(vm.text) { isLoading, text -> !isLoading && (allowEmpty || text.isNotEmpty()) }
            .launchCollect(viewLifecycleOwner.lifecycleScope) {
                menu.findItem(R.id.done)?.isEnabled = it
            }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.done -> launch { onDone() }
            else -> return false
        }

        return true
    }

    open suspend fun onDone() {
        findNavController().navigateUp()
    }
}
