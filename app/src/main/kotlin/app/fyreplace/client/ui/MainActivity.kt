package app.fyreplace.client.ui

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import android.view.ViewGroup
import android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import app.fyreplace.client.R
import app.fyreplace.client.databinding.ActivityMainBinding
import app.fyreplace.client.viewmodels.CentralViewModel
import app.fyreplace.client.viewmodels.MainViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.tabs.TabLayout
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity :
    AppCompatActivity(R.layout.activity_main),
    FailureHandler,
    FragmentOnAttachListener,
    NavController.OnDestinationChangedListener,
    TabLayout.OnTabSelectedListener {
    override val rootView get() = bd.root
    override val preferences by inject<SharedPreferences>()
    private val vm by viewModel<MainViewModel>()
    private val cvm by viewModel<CentralViewModel>()
    private lateinit var bd: ActivityMainBinding
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
        navHost.navController.addOnDestinationChangedListener(this)
        bd.tabs.addOnTabSelectedListener(this)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        vm.pageState.launchCollect { state ->
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

        cvm.isAuthenticated.launchCollect { authenticated ->
            cvm.retrieveMe()

            for (destination in AUTHENTICATED_DESTINATIONS) {
                bd.bottomNavigation.menu.findItem(destination).isEnabled = authenticated
            }

            if (!authenticated && navHost.navController.currentDestination?.id in AUTHENTICATED_DESTINATIONS) {
                navHost.navController.navigate(R.id.fragment_settings)
            }
        }
    }

    override fun onDestroy() {
        navHost.childFragmentManager.removeFragmentOnAttachListener(this)
        navHost.navController.removeOnDestinationChangedListener(this)
        bd.tabs.removeOnTabSelectedListener(this)
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navHost.navController.handleDeepLink(intent)
    }

    override fun onSupportNavigateUp() =
        navHost.navController.navigateUp() || super.onSupportNavigateUp()

    override fun onAttachFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        vm.setPageChoices(if (fragment is PageChoosing) fragment.pageChoices else emptyList())

        if (fragment is TitleChoosing) {
            bd.toolbar.setTitle(fragment.getTitle())
        }
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) = setToolbarInfo(null, "", "")

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

    fun setToolbarInfo(title: String?, subtitle: String?, icon: String?) {
        title?.let { bd.toolbar.title = getUsername(it) }
        subtitle?.let { bd.toolbar.subtitle = it }
        icon?.let {
            if (it.isEmpty()) {
                bd.toolbar.logo = null
            } else {
                val avatarSize = resources.getDimensionPixelSize(R.dimen.avatar_size)
                Glide.with(this).loadAvatar(icon).into(LogoTarget(avatarSize))
            }
        }
    }

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
        val AUTHENTICATED_DESTINATIONS = setOf(
            R.id.fragment_notifications,
            R.id.fragment_archive,
            R.id.fragment_drafts,
        )
    }

    private inner class LogoTarget(private val size: Int) : CustomTarget<Drawable>() {
        override fun onResourceReady(
            resource: Drawable,
            transition: Transition<in Drawable>?
        ) {
            bd.toolbar.logo = resource
            bd.toolbar.children
                .filterIsInstance<ImageView>()
                .find { it.drawable == resource }
                ?.run {
                    updateLayoutParams<ViewGroup.LayoutParams> {
                        width = size
                        height = size
                    }
                }
        }

        override fun onLoadCleared(placeholder: Drawable?) = Unit

        override fun onLoadFailed(errorDrawable: Drawable?) {
            errorDrawable?.let { onResourceReady(it, null) }
        }
    }
}
