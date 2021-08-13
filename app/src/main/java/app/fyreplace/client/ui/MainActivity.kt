package app.fyreplace.client.ui

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import app.fyreplace.client.R
import app.fyreplace.client.databinding.ActivityMainBinding
import app.fyreplace.client.viewmodels.MainViewModel
import com.google.android.material.tabs.TabLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(R.layout.activity_main), FragmentOnAttachListener,
    TabLayout.OnTabSelectedListener {
    private lateinit var bd: ActivityMainBinding
    private val vm by viewModel<MainViewModel>()
    private lateinit var navHost: NavHostFragment
    private var skipNextTabChange = true

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setBackgroundDrawable(null)
        super.onCreate(savedInstanceState)
        bd = ActivityMainBinding.bind(findViewById(R.id.root)).also {
            it.lifecycleOwner = this
            it.vm = vm
        }

        setSupportActionBar(bd.toolbar)
        setupSystemBars()

        val appBarConfiguration = AppBarConfiguration(TOP_LEVEL_DESTINATIONS)
        navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        setupActionBarWithNavController(navHost.navController, appBarConfiguration)
        bd.bottomNavigation.setupWithNavController(navHost.navController)

        navHost.childFragmentManager.addFragmentOnAttachListener(this)
        bd.tabs.addOnTabSelectedListener(this)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        vm.pageState.observe(this) { state ->
            skipNextTabChange = true
            bd.tabs.removeAllTabs()

            for (title in state.choices) {
                val tab = bd.tabs.newTab()
                bd.tabs.addTab(tab)
                tab.setText(title)

                if (tab.position == state.current) {
                    bd.tabs.selectTab(tab)
                }
            }
        }
    }

    override fun onDestroy() {
        navHost.childFragmentManager.removeFragmentOnAttachListener(this)
        bd.tabs.removeOnTabSelectedListener(this)
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navHost.navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onAttachFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        vm.setPageChoices(if (fragment is PageChoosing) fragment.pageChoices else emptyList())

        if (fragment is TitleChoosing) {
            bd.toolbar.setTitle(fragment.getTitle())
        }
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        if (skipNextTabChange) {
            skipNextTabChange = false
            return
        }

        vm.choosePage(tab.position)
        navHost.childFragmentManager.fragments
            .mapNotNull { it as? PageChoosing }
            .last()
            .choosePage(tab.position)
    }

    override fun onTabUnselected(tab: TabLayout.Tab) = Unit

    override fun onTabReselected(tab: TabLayout.Tab) = Unit

    private fun setupSystemBars() {
        window.statusBarColor = ActivityCompat.getColor(this, R.color.primary_dark)
        window.navigationBarColor = ActivityCompat.getColor(this, R.color.navigation)
        val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        when {
            nightMode == Configuration.UI_MODE_NIGHT_YES -> return
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
                bd.root.windowInsetsController?.setSystemBarsAppearance(
                    APPEARANCE_LIGHT_NAVIGATION_BARS,
                    APPEARANCE_LIGHT_NAVIGATION_BARS
                )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
                @Suppress("deprecation")
                bd.root.systemUiVisibility =
                    bd.root.systemUiVisibility or SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
    }

    private companion object {
        val TOP_LEVEL_DESTINATIONS = setOf(
            R.id.fragment_feed,
            R.id.fragment_notifications,
            R.id.fragment_archive,
            R.id.fragment_drafts,
            R.id.fragment_settings,
        )
    }
}
