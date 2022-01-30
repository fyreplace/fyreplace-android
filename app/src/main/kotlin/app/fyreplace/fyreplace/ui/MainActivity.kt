package app.fyreplace.fyreplace.ui

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
import app.fyreplace.fyreplace.grpc.isAvailable
import app.fyreplace.fyreplace.viewmodels.CentralViewModel
import app.fyreplace.fyreplace.viewmodels.MainViewModel
import app.fyreplace.protos.Profile
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.color.DynamicColors
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity :
    AppCompatActivity(R.layout.activity_main),
    FailureHandler,
    FragmentOnAttachListener,
    NavController.OnDestinationChangedListener {
    override val rootView by lazy { bd.root }
    private val vm by viewModel<MainViewModel>()
    private val cvm by viewModel<CentralViewModel>()
    private lateinit var bd: ActivityMainBinding
    private lateinit var navHost: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        DynamicColors.applyIfAvailable(this)
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
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navHost.navController.handleDeepLink(intent)
    }

    override fun onSupportNavigateUp() =
        navHost.navController.navigateUp() || super.onSupportNavigateUp()

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
    }

    fun setToolbarInfo(profile: Profile?, subtitle: String?) {
        profile?.let { bd.toolbar.title = getUsername(it) }
        bd.toolbar.subtitle = subtitle
        val textViews = bd.toolbar.children.filterIsInstance<TextView>()

        if (profile?.isAvailable == true) {
            val avatarSize = resources.getDimensionPixelSize(R.dimen.avatar_size)
            Glide.with(this)
                .loadAvatar(profile.avatar.url)
                .into(LogoTarget(avatarSize, profile))

            for (view in textViews) {
                view.setOnClickListener {
                    navHost.navController.navigate(MainDirections.actionUser(profile = profile))
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
                        navHost.navController.navigate(MainDirections.actionUser(profile = profile))
                    }
                }
        }

        override fun onLoadCleared(placeholder: Drawable?) = Unit

        override fun onLoadFailed(errorDrawable: Drawable?) {
            errorDrawable?.let { onResourceReady(it, null) }
        }
    }
}
