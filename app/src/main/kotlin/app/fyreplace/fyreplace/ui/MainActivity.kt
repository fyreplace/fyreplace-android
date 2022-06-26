package app.fyreplace.fyreplace.ui

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
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
import app.fyreplace.fyreplace.MainDirections
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.ActivityMainBinding
import app.fyreplace.fyreplace.extensions.byteString
import app.fyreplace.fyreplace.extensions.getUsername
import app.fyreplace.fyreplace.extensions.isAvailable
import app.fyreplace.fyreplace.extensions.loadAvatar
import app.fyreplace.fyreplace.grpc.p
import app.fyreplace.fyreplace.viewmodels.CentralViewModel
import app.fyreplace.fyreplace.viewmodels.MainViewModel
import app.fyreplace.protos.Profile
import app.fyreplace.protos.post
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.color.DynamicColors
import io.grpc.Status
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity :
    AppCompatActivity(R.layout.activity_main),
    FailureHandler,
    FragmentOnAttachListener,
    NavController.OnDestinationChangedListener {
    override val rootView by lazy { if (this::bd.isInitialized) bd.root else null }
    private val vm by viewModel<MainViewModel>()
    private val cvm by viewModel<CentralViewModel>()
    private lateinit var bd: ActivityMainBinding
    private lateinit var navHost: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        DynamicColors.applyToActivityIfAvailable(this)
        super.onCreate(savedInstanceState)
        bd = ActivityMainBinding.bind(findViewById(R.id.root))
        bd.lifecycleOwner = this

        setSupportActionBar(bd.toolbar)

        val appBarConfiguration = AppBarConfiguration(TOP_LEVEL_DESTINATIONS)
        navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        setupActionBarWithNavController(navHost.navController, appBarConfiguration)
        bd.bottomNavigation.setupWithNavController(navHost.navController)

        navHost.childFragmentManager.addFragmentOnAttachListener(this)
        navHost.navController.addOnDestinationChangedListener(this)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        cvm.isAuthenticated.launchCollect { authenticated ->
            for (destination in AUTHENTICATED_DESTINATIONS) {
                bd.bottomNavigation.menu.findItem(destination).isEnabled = authenticated
            }

            if (!authenticated && navHost.navController.currentDestination?.id in AUTHENTICATED_DESTINATIONS) {
                navHost.navController.navigate(R.id.fragment_settings)
            }

            launch { cvm.retrieveMe() }
        }

        handleIntent(intent)
    }

    override fun onDestroy() {
        navHost.childFragmentManager.removeFragmentOnAttachListener(this)
        navHost.navController.removeOnDestinationChangedListener(this)
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onSupportNavigateUp() =
        navHost.navController.navigateUp() || super.onSupportNavigateUp()

    override fun getContext() = this

    override fun getFailureTexts(error: Status) = when (error.code) {
        Status.Code.UNAUTHENTICATED -> when (error.description) {
            "timestamp_exceeded" -> R.string.main_error_timestamp_exceeded_title to R.string.main_error_timestamp_exceeded_message
            "invalid_token" -> R.string.main_error_invalid_token_title to R.string.main_error_invalid_token_message
            else -> R.string.error_authentication_title to R.string.error_authentication_message
        }
        Status.Code.PERMISSION_DENIED -> when (error.description) {
            "invalid_connection_token" -> R.string.main_error_invalid_connection_token_title to R.string.main_error_invalid_connection_token_message
            "user_not_pending" -> R.string.main_error_user_not_pending_title to R.string.main_error_user_not_pending_message
            else -> R.string.error_permission_title to R.string.error_permission_message
        }
        else -> super.getFailureTexts(error)
    }

    override fun onAttachFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        if (fragment is TitleChoosing) {
            bd.toolbar.setTitle(fragment.getTitle())
        }
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        if (destination.id in TOP_LEVEL_DESTINATIONS) {
            setToolbarInfo(null, null)
        }

        bd.primaryAction.hide()
    }

    fun setToolbarInfo(title: String?) = bd.toolbar.run {
        this.title = title
        isTitleCentered = false
        subtitle = null
        logo = null

        for (view in children.filterIsInstance<TextView>()) {
            view.setOnClickListener(null)
        }
    }

    fun setToolbarInfo(profile: Profile?, subtitle: String?) {
        profile?.let { bd.toolbar.title = it.getUsername(this) }
        bd.toolbar.subtitle = subtitle
        val textViews = bd.toolbar.children.filterIsInstance<TextView>()

        if (profile?.isAvailable == true) {
            val avatarSize = resources.getDimensionPixelSize(R.dimen.avatar_size)
            Glide.with(this)
                .loadAvatar(profile)
                .into(LogoTarget(avatarSize, profile))

            for (view in textViews) {
                view.setOnClickListener {
                    navHost.navController.navigate(MainDirections.actionUser(profile = profile.p))
                }
            }
        } else {
            bd.toolbar.logo = null

            for (view in textViews) {
                view.setOnClickListener(null)
            }
        }

        bd.toolbar.isTitleCentered = profile == null
        bd.toolbar.isSubtitleCentered = bd.toolbar.isTitleCentered
    }

    fun setPrimaryAction(@DrawableRes icon: Int, listener: View.OnClickListener) {
        bd.primaryAction.setImageResource(icon)
        bd.primaryAction.setOnClickListener(listener)
        bd.primaryAction.show()
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        val token = uri.fragment ?: ""
        this.intent = null

        when (val path = uri.path ?: return) {
            getString(R.string.link_path_account_confirm_activation) -> confirmActivation(token)
            getString(R.string.link_path_account_confirm_connection) -> confirmConnection(token)
            getString(R.string.link_path_user_confirm_email_update) -> confirmEmailUpdate(token)
            else -> when {
                path.startsWith("/p/") -> showPost(path.drop(3))
                else -> showBasicAlert(
                    R.string.main_error_malformed_url_title,
                    R.string.main_error_malformed_url_message,
                    error = true
                )
            }
        }
    }

    private fun confirmActivation(token: String) = launch(autoDisconnect = false) {
        vm.confirmActivation(token)
        showBasicSnackbar(R.string.main_account_activated_message)
    }

    private fun confirmConnection(token: String) = launch(autoDisconnect = false) {
        vm.confirmConnection(token)
    }

    private fun confirmEmailUpdate(token: String) = launch(autoDisconnect = false) {
        vm.confirmEmailUpdate(token)
        cvm.retrieveMe()
        showBasicSnackbar(R.string.main_user_email_changed_message)
    }

    private fun showPost(postIdShortString: String) {
        val post = post { id = byteString(postIdShortString) }
        val directions = MainDirections.actionPost(post = post.p)
        navHost.navController.navigate(directions)
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

    private inner class LogoTarget(private val size: Int, private val profile: Profile) :
        CustomTarget<Drawable>() {
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

                    with(TypedValue()) {
                        context.theme.resolveAttribute(
                            R.attr.selectableItemBackgroundBorderless,
                            this,
                            true
                        )
                        setBackgroundResource(resourceId)
                    }

                    setOnClickListener {
                        navHost.navController.navigate(MainDirections.actionUser(profile = profile.p))
                    }
                }
        }

        override fun onLoadCleared(placeholder: Drawable?) = Unit

        override fun onLoadFailed(errorDrawable: Drawable?) {
            errorDrawable?.let { onResourceReady(it, null) }
        }
    }
}
