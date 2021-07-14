package com.leschnitzky.dailyshiba

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.bumptech.glide.Glide
import com.leschnitzky.dailyshiba.usermanagement.ui.UserViewModel
import com.leschnitzky.dailyshiba.utils.DEFAULT_PROFILE_PIC
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import timber.log.Timber


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    val scope = CoroutineScope(Job() + Dispatchers.Main + CoroutineName("MainActivity"))

    private val TAG = "MainActivity"
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var listener : NavigationView.OnNavigationItemSelectedListener

    private lateinit var appBarConfiguration: AppBarConfiguration


    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d( "onCreate: ")
        super.onCreate(savedInstanceState)
        listener = this
        MobileAds.initialize(this) {}
        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(listOf(getString(R.string.phone_imei))).build()
        MobileAds.setRequestConfiguration(configuration)
        actionBar?.setDisplayHomeAsUpEnabled(false)


        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        observeUserStatesForDrawer(navController,navView)


        navController.addOnDestinationChangedListener { _, _, _ ->
              supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_hamburger)
        }
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_login_menu, R.id.nav_shiba,
            R.id.nav_logoff, R.id.nav_main, R.id.nav_favorites, R.id.nav_profile
        ), drawerLayout)

        setupActionBarWithNavController(navController, drawerLayout)
        navView.setupWithNavController(navController)

        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

    }

    private fun observeUserStatesForDrawer(navController: NavController, navView: NavigationView) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                Timber.d( "checkUserStatus: Observing on ${this.coroutineContext}")
                userViewModel.mainActivityUIState.collect {
                    Timber.d( "checkUserStatus: Got $it")
                    handleUserUIChangeInMainActivity(it, navView, navController)
                }
            }
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        Timber.d( "onDestroy: ")
        super.onDestroy()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_logoff -> {
                Timber.d( "onNavigationItemSelected: Pressed!")
                userViewModel.viewModelScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED){
                        Timber.d( "onNavigationItemSelected: Pressed from Coroutine")
                        userViewModel.intentChannel.send(UserIntent.LogoutUser)
                    }
                }
            }
            R.id.nav_favorites -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.nav_favorites)
            }
            R.id.nav_shiba -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.nav_shiba)
            }
            R.id.nav_register -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.nav_register)
            }

            R.id.nav_profile -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.nav_profile)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun handleUserUIChangeInMainActivity(
        it: MainContract.State,
        navView: NavigationView,
        navController: NavController
    ) {
        when (it.titleState) {

            is MainContract.UserTitleState.Member -> {
                navView
                    .getHeaderView(0)
                    .findViewById<TextView>(R.id.drawer_title)
                    .text = it.titleState.name

                navView.menu.setGroupVisible(R.id.member, true)
                navView.menu.setGroupVisible(R.id.unsigned, false)
                Timber.d( "observeUserTitleState: moving to SHIBA")
                navController.navigate(
                    R.id.nav_shiba,
                    bundleOf("name" to it.titleState.name)
                )
                navController.graph.startDestination = R.id.nav_shiba


            }
            is MainContract.UserTitleState.Guest -> {
                navView.menu.setGroupVisible(R.id.member, false)
                navView.menu.setGroupVisible(R.id.unsigned, true)
                navView
                    .getHeaderView(0)
                    .findViewById<TextView>(R.id.drawer_title)
                    .text = resources.getString(R.string.initial_user_title)
                Timber.d( "observeUserTitleState: moving to LOGIN")
                navController.navigate(R.id.nav_login_menu)

                navController.graph.startDestination = R.id.nav_login_menu

            }
            is MainContract.UserTitleState.InitState -> {
            }
            is MainContract.UserTitleState.MemberNoNavigate -> {
                navView.menu.setGroupVisible(R.id.member, true)
                navView.menu.setGroupVisible(R.id.unsigned, false)
                navView
                    .getHeaderView(0)
                    .findViewById<TextView>(R.id.drawer_title)
                    .text = it.titleState.displayName
                navController.graph.startDestination = R.id.nav_shiba
            }
        }


        when(it.profilePicState){
            is MainContract.UserProfilePicState.DefaultPicture -> {
                Glide
                    .with(this)
                    .asDrawable()
                    .load(DEFAULT_PROFILE_PIC)
                    .override(200,200)
                    .into(
                        findViewById<NavigationView>(R.id.nav_view).
                        getHeaderView(0).
                        findViewById(R.id.drawer_profile_pic))

            }
            is MainContract.UserProfilePicState.NewProfilePic -> {
                Glide
                    .with(this)
                    .asDrawable()
                    .load(it.profilePicState.picture)
                    .override(200,200)
                    .into(
                        findViewById<NavigationView>(R.id.nav_view).
                        getHeaderView(0).
                        findViewById(R.id.drawer_profile_pic))
            }
            else -> {}
        }


    }



}