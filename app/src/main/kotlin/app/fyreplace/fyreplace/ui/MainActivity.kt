package app.fyreplace.fyreplace.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import androidx.lifecycle.whenStarted
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import app.fyreplace.fyreplace.MainDirections
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.ActivityMainBinding
import app.fyreplace.fyreplace.events.CommentSeenEvent
import app.fyreplace.fyreplace.events.EventsManager
import app.fyreplace.fyreplace.extensions.*
import app.fyreplace.fyreplace.grpc.p
import app.fyreplace.fyreplace.ui.fragments.PostFragment
import app.fyreplace.fyreplace.viewmodels.CentralViewModel
import app.fyreplace.fyreplace.viewmodels.MainViewModel
import app.fyreplace.protos.Profile
import app.fyreplace.protos.post
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.color.DynamicColors
import com.google.android.material.shape.MaterialShapeDrawable
import dagger.hilt.android.AndroidEntryPoint
import io.grpc.Status
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class MainActivity :
    AppCompatActivity(R.layout.activity_main),
    FailureHandler,
    FragmentManager.OnBackStackChangedListener,
    FragmentOnAttachListener,
    NavController.OnDestinationChangedListener {
    @Inject
    lateinit var em: EventsManager

    override val rootView get() = if (::bd.isInitialized) bd.root else null
    private val vm by viewModels<MainViewModel>()
    private val cvm by viewModels<CentralViewModel>()
    private lateinit var bd: ActivityMainBinding
    private lateinit var navHost: NavHostFragment
    private var defaultNavigationBarDividerColor = Color.TRANSPARENT
    private var bottomBarHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        DynamicColors.applyToActivityIfAvailable(this)
        super.onCreate(savedInstanceState)
        bd = ActivityMainBinding.bind(findViewById(R.id.root)).apply {
            lifecycleOwner = this@MainActivity
            ui = this@MainActivity
        }

        navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navHost.childFragmentManager.addOnBackStackChangedListener(this)
        navHost.childFragmentManager.addFragmentOnAttachListener(this)
        navHost.navController.addOnDestinationChangedListener(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            defaultNavigationBarDividerColor = window.navigationBarDividerColor
        }

        setNavigationBarColor(true)
        setSupportActionBar(bd.toolbar)

        val appBarConfiguration = AppBarConfiguration(TOP_LEVEL_DESTINATIONS)
        setupActionBarWithNavController(navHost.navController, appBarConfiguration)
        bd.bottomNavigation.setupWithNavController(navHost.navController)
        bd.bottomNavigation.doOnLayout { bottomBarHeight = it.height }
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

        em.events.filterIsInstance<CommentSeenEvent>().launchCollect {
            vm.acknowledgeComment(it.item.id)
        }

        refreshCustomTitle()
        refreshPrimaryAction()
        handleIntent(intent)
    }

    override fun onDestroy() {
        navHost.childFragmentManager.removeOnBackStackChangedListener(this)
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

    override fun onBackStackChanged() {
        refreshCustomTitle()
        refreshPrimaryAction()
        launch {
            navHost.childFragmentManager.fragments.last().whenStarted {
                delay(100)
                bd.appBar.liftOnScrollTargetViewId = R.id.recycler_view
                val destination = navHost.navController.currentDestination
                setBottomNavigationVisible(destination?.isTopLevel == true)
            }
        }
    }

    override fun onAttachFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        if (fragment is TitleProvider) {
            bd.toolbar.setTitle(fragment.getTitle())
        }
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        removeCustomTitle()
        bd.toolbar.isTitleCentered = destination.isTopLevel

        if (destination.isTopLevel) {
            setToolbarInfo(null, null)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onPrimaryActionClicked(view: View) {
        getPrimaryActionProvider()?.onPrimaryAction()
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

    fun refreshCustomTitle() {
        val fragment = getCustomTitleProvider() ?: return removeCustomTitle()
        setCustomTitle(fragment.getCustomTitleView())
    }

    fun refreshPrimaryAction() {
        val fragment = getPrimaryActionProvider() ?: return removePrimaryAction()
        val style = fragment.getPrimaryActionStyle()
        val text =
            if (style == PrimaryActionStyle.EXTENDED) fragment.getPrimaryActionText()
            else null
        val icon =
            if (style != PrimaryActionStyle.NONE) fragment.getPrimaryActionIcon()
            else null

        if (text == null && icon == null) {
            removePrimaryAction()
        } else {
            setPrimaryAction(text, icon)
        }
    }

    private fun setCustomTitle(view: View) {
        setToolbarInfo(null)
        removeCustomTitle()
        bd.customTitle.addView(view)
    }

    private fun removeCustomTitle() = bd.customTitle.removeAllViews()

    private fun setPrimaryAction(
        @StringRes text: Int?,
        @DrawableRes icon: Int?
    ) = with(bd.primaryAction) {
        icon?.let { setIconResource(it) }

        if (text != null) {
            setText(text)
            extend()
        } else {
            shrink()
        }

        show()
    }

    private fun removePrimaryAction() = with(bd.primaryAction) {
        shrink()
        hide()
    }

    private fun getCustomTitleProvider() =
        navHost.childFragmentManager.fragments.last { it !is DialogFragment } as? CustomTitleProvider

    private fun getPrimaryActionProvider() =
        navHost.childFragmentManager.fragments.last { it !is DialogFragment } as? PrimaryActionProvider

    private fun getPostFragment() =
        navHost.childFragmentManager.fragments.last() as? PostFragment

    private fun setBottomNavigationVisible(visible: Boolean) = bd.bottomNavigation.doOnLayout {
        val params = bd.bottomNavigation.layoutParams as ViewGroup.MarginLayoutParams
        val isBottomNavigationVisible = params.bottomMargin == 0

        if (visible == isBottomNavigationVisible && bd.bottomNavigation.animation == null) {
            return@doOnLayout
        }

        val animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                val factor = if (visible) interpolatedTime - 1 else -interpolatedTime
                params.bottomMargin = (bottomBarHeight * factor).roundToInt()
                bd.bottomNavigation.layoutParams = params
            }
        }
        animation.duration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                if (visible) {
                    setNavigationBarColor(true)
                }
            }

            override fun onAnimationEnd(animation: Animation?) {
                if (!visible) {
                    setNavigationBarColor(false)
                }
            }

            override fun onAnimationRepeat(animation: Animation?) = Unit
        })
        bd.bottomNavigation.clearAnimation()
        bd.bottomNavigation.startAnimation(animation)
    }

    private fun setNavigationBarColor(colored: Boolean) {
        val background = bd.bottomNavigation.background

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1 || background !is MaterialShapeDrawable) {
            return
        }

        window.navigationBarColor = if (colored)
            background.resolvedTintColor
        else
            getDynamicColor(R.attr.colorSurface, getColor(R.color.navigation))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.navigationBarDividerColor = if (colored)
                getDynamicColor(R.attr.colorSurfaceVariant, window.navigationBarDividerColor)
            else
                defaultNavigationBarDividerColor
        }
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
                path.startsWith("/p/") -> {
                    val parts = path.drop(3).split('/')
                    showPost(parts.first(), parts.getOrNull(1)?.toIntOrNull())
                }
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

    private fun showPost(postIdShortString: String, commentPosition: Int? = null) {
        val post = post { id = byteString(postIdShortString) }
        val postFragment = getPostFragment()

        when {
            post.id.isEmpty -> showBasicAlert(
                R.string.main_error_malformed_url_title,
                R.string.main_error_malformed_url_message,
                error = true
            )
            !cvm.isAuthenticated.value -> showBasicAlert(
                R.string.error_authentication_title,
                R.string.error_authentication_message,
                error = true
            )
            postFragment == null || commentPosition == null || !postFragment.tryShowComment(
                post.id, commentPosition
            ) -> navHost.navController.navigate(
                MainDirections.actionPost(
                    post = post.p,
                    commentPosition = commentPosition ?: -1
                )
            )
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

    private val NavDestination.isTopLevel get() = id in TOP_LEVEL_DESTINATIONS
}
