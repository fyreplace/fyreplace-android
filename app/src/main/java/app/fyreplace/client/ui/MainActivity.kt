package app.fyreplace.client.ui

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import app.fyreplace.client.R
import app.fyreplace.client.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var bd: ActivityMainBinding
    private lateinit var navHost: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setBackgroundDrawable(null)
        super.onCreate(savedInstanceState)
        bd = ActivityMainBinding.bind(findViewById(R.id.root)).apply {
            lifecycleOwner = this@MainActivity
        }

        navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        bd.bottomNavigation.setupWithNavController(navHost.navController)
        setSupportActionBar(bd.toolbar)
        setupSystemBars()
        reportFullyDrawn()
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
}
