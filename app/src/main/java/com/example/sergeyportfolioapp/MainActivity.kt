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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.bumptech.glide.Glide
import com.example.sergeyportfolioapp.usermanagement.ui.UserIntent
import com.example.sergeyportfolioapp.usermanagement.ui.UserProfilePicState
import com.example.sergeyportfolioapp.usermanagement.ui.UserTitleState
import com.example.sergeyportfolioapp.usermanagement.ui.UserViewModel
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val TAG = "MainActivity"
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var drawerLayout : DrawerLayout

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: ")
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)

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

        observeUserStatesForDrawer(navController,navView)
    }

    private fun observeUserStatesForDrawer(navController: NavController, navView: NavigationView) {
        lifecycleScope.launchWhenStarted {
            observeUserTitleState(navController, navView)
        }

        lifecycleScope.launchWhenStarted {
            observeProfileViewState()
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
                lifecycleScope.launch {
                    userViewModel._intentChannel.send(UserIntent.LogoutUser)
                }
            }
        }
        //close navigation drawer
        //close navigation drawer
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


    private fun observeUserTitleState(navController : NavController, navView : NavigationView) {
        userViewModel.userTitle.onEach {
            handleUserTitleChange(it,navView,navController)
        }.catch {  }
    }

    private fun handleUserTitleChange(
        it: UserTitleState,
        navView: NavigationView,
        navController: NavController
    ) {
        when (it) {
            is UserTitleState.Member -> {
                navView
                    .getHeaderView(0)
                    .findViewById<TextView>(R.id.drawer_title)
                    .text = it.name

                navView.menu.setGroupVisible(R.id.member, true)
                navView.menu.setGroupVisible(R.id.unsigned, false)
                navController.graph.startDestination = R.id.nav_shiba
                Log.d(TAG, "observeUserTitleState: moving to SHIBA")
                navController.navigate(
                    R.id.nav_shiba,
                    bundleOf("name" to it.name)
                )

            }
            is UserTitleState.Guest -> {
                navView.menu.setGroupVisible(R.id.member, false)
                navView.menu.setGroupVisible(R.id.unsigned, true)
                navView
                    .getHeaderView(0)
                    .findViewById<TextView>(R.id.drawer_title)
                    .text = resources.getString(R.string.initial_user_title)
                navController.graph.startDestination =
                    R.id.nav_login_menu

                Log.d(TAG, "observeUserTitleState: moving to LOGIN")
                navController.navigate(R.id.nav_login_menu)
            }
            is UserTitleState.InitState -> {
            }
        }

    }

    suspend fun observeProfileViewState() {
        userViewModel.currentProfilePicture.onEach {
            handleProfilePictureChange(it)
        }.catch {  }
    }

    private fun handleProfilePictureChange(it: UserProfilePicState) {
        when(it){
            is UserProfilePicState.DefaultPicture -> {
                Glide
                    .with(this)
                    .asDrawable()
                    .load("http://cdn.shibe.online/shibes/1dceabce914325b357fbd59e4ef829bc5ddfad6c.jpg")
                    .override(200,200)
                    .into(
                        findViewById<NavigationView>(R.id.nav_view).
                        getHeaderView(0).
                        findViewById(R.id.drawer_profile_pic))

            }
            is UserProfilePicState.NewProfilePic -> {
                Glide
                    .with(this)
                    .asDrawable()
                    .load(it.picture)
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