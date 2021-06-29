package com.example.sergeyportfolioapp

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
import com.example.sergeyportfolioapp.usermanagement.ui.UserViewModel
import com.example.sergeyportfolioapp.utils.DEFAULT_PROFILE_PIC
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect


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
        Log.d(TAG, "onCreate: ")
        super.onCreate(savedInstanceState)
        listener = this
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
            R.id.nav_logoff, R.id.nav_main
        ), drawerLayout)

        setupActionBarWithNavController(navController, drawerLayout)
        navView.setupWithNavController(navController)

        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

    }

    private fun observeUserStatesForDrawer(navController: NavController, navView: NavigationView) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                Log.d(TAG, "checkUserStatus: Observing on ${this.coroutineContext}")
                userViewModel.mainActivityUIState.collect {
                    Log.d(TAG, "checkUserStatus: Got $it")
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
        Log.d(TAG, "onDestroy: ")
        super.onDestroy()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_logoff -> {
                Log.d(TAG, "onNavigationItemSelected: Pressed!")
                userViewModel.viewModelScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED){
                        Log.d(TAG, "onNavigationItemSelected: Pressed from Coroutine")
                        userViewModel.intentChannel.send(UserIntent.LogoutUser)
                    }
                }
            }
            R.id.nav_register -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.nav_register)
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
                Log.d(TAG, "observeUserTitleState: moving to SHIBA")
                navController.graph.startDestination = R.id.nav_shiba

                navController.navigate(
                    R.id.nav_shiba,
                    bundleOf("name" to it.titleState.name)
                )


            }
            is MainContract.UserTitleState.Guest -> {
                navView.menu.setGroupVisible(R.id.member, false)
                navView.menu.setGroupVisible(R.id.unsigned, true)
                navView
                    .getHeaderView(0)
                    .findViewById<TextView>(R.id.drawer_title)
                    .text = resources.getString(R.string.initial_user_title)
                Log.d(TAG, "observeUserTitleState: moving to LOGIN")
                navController.navigate(R.id.nav_login_menu).also {
                    navController.graph.startDestination = R.id.nav_login_menu
                }

            }
            is MainContract.UserTitleState.InitState -> {
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