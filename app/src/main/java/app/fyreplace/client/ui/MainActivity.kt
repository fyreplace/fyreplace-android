package app.fyreplace.client.ui

import android.content.Intent
import android.content.SharedPreferences
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
import app.fyreplace.client.viewmodels.CentralViewModel
import app.fyreplace.client.viewmodels.MainViewModel
import com.google.android.material.tabs.TabLayout
import io.grpc.Status
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity :
    AppCompatActivity(R.layout.activity_main),
    FailureHandler,
    FragmentOnAttachListener,
    TabLayout.OnTabSelectedListener {
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
        bd.tabs.addOnTabSelectedListener(this)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        vm.getUsableIntent(intent)?.let(::handleIntent)
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

        cvm.isAuthenticated.launchCollect { cvm.retrieveMe() }
    }

    override fun onDestroy() {
        navHost.childFragmentManager.removeFragmentOnAttachListener(this)
        bd.tabs.removeOnTabSelectedListener(this)
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navHost.navController.handleDeepLink(intent)
        vm.getUsableIntent(intent)?.let { handleIntent(it) }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navHost.navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun getContext() = this

    override fun onFailure(failure: Throwable) {
        val error = Status.fromThrowable(failure)
        val (title, message) = when (error.code) {
            Status.Code.UNAUTHENTICATED -> when (error.description) {
                "timestamp_exceeded" -> R.string.main_error_timestamp_exceeded_title to R.string.main_error_timestamp_exceeded_message
                "invalid_token" -> R.string.main_error_invalid_token_title to R.string.main_error_invalid_token_message
                else -> R.string.error_authentication_title to R.string.error_authentication_message
            }
            Status.Code.PERMISSION_DENIED -> when (error.description) {
                "user_not_pending" -> R.string.main_error_user_not_pending_title to R.string.main_error_user_not_pending_message
                else -> R.string.error_permission_title to R.string.error_permission_message
            }
            else -> return super.onFailure(failure)
        }

        showBasicAlert(title, message, error = true)
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

    private fun handleIntent(intent: Intent) {
        val uri = intent.data ?: return

        if (uri.scheme != "fyreplace" || !uri.host.isNullOrEmpty()) {
            return showBasicAlert(
                R.string.main_error_malformed_url_title,
                R.string.main_error_malformed_url_message,
                error = true
            )
        }

        when (uri.path) {
            "/AccountService.ConfirmActivation" -> confirmActivation(uri.fragment.orEmpty())
            else -> showBasicAlert(
                R.string.main_error_malformed_url_title,
                R.string.main_error_malformed_url_message,
                error = true
            )
        }
    }

    private fun confirmActivation(token: String) = launch {
        vm.confirmActivation(token)
        showBasicAlert(
            R.string.main_account_activated_title,
            R.string.main_account_activated_message
        )
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
