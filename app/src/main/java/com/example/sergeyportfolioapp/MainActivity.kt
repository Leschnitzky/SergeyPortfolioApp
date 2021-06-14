package com.example.sergeyportfolioapp

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.sergeyportfolioapp.usermanagement.ui.UserTitleState
import com.example.sergeyportfolioapp.usermanagement.ui.UserViewModel
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlin.coroutines.CoroutineContext
import kotlin.math.log


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var mJob: Job
    override val coroutineContext: CoroutineContext
        get() = mJob + Dispatchers.Main

    private val TAG = "MainActivity"
    private val userViewModel: UserViewModel by viewModels()


    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: ")
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_main)

        mJob = Job()
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)



        navController.addOnDestinationChangedListener { _, _, _ ->
              supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_hamburger)
        }
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_login_menu, R.id.nav_shiba
        ), drawerLayout)

        launch {
            navView.setNavigationItemSelectedListener { item ->
                item.isChecked = true
                when(item.itemId) {
                    R.id.nav_logoff -> {
                            userViewModel.logoutUser()
                        navController.graph.startDestination = R.id.nav_login_menu
                        navController.navigate(R.id.nav_login_menu)
                    }
                    R.id.nav_login_menu -> {
                        navController.navigate(R.id.nav_login_menu)
                    }
                    R.id.nav_register -> {
                        navController.navigate(R.id.nav_register)
                    }
                }
                drawerLayout.closeDrawers()
                true
            }
        }

        lifecycleScope.launchWhenStarted {

            userViewModel.userTitle.collect() {

                Log.d(TAG, "onCreate: Got $it")
                when(it){
                    is UserTitleState.Member -> {
                        navView
                            .getHeaderView(0)
                            ?.findViewById<TextView>(R.id.drawer_title)
                            ?.text = it.name

                        navView!!.menu.setGroupVisible(R.id.member,true)
                        navView!!.menu.setGroupVisible(R.id.unsigned,false)
                        navController.graph.startDestination = R.id.nav_shiba
                        navController.navigate(
                            R.id.nav_shiba,
                            bundleOf("name" to it.name)
                        )

                    }
                    is UserTitleState.Guest -> {
                        navView!!.menu.setGroupVisible(R.id.member,false)
                        navView!!.menu.setGroupVisible(R.id.unsigned,true)
                        navView
                            .getHeaderView(0)
                            ?.findViewById<TextView>(R.id.drawer_title)
                            ?.text = resources.getString(R.string.initial_user_title)
                        findNavController(R.id.nav_host_fragment).graph.startDestination = R.id.nav_login_menu
                        findNavController(R.id.nav_host_fragment).navigate(R.id.nav_login_menu)
                    }
                    is UserTitleState.InitState -> { }
                }

                setupActionBarWithNavController(navController, drawerLayout)

            }

        }
        userViewModel.checkIfUserLoggedIn()


        if(userViewModel.getCurrentUserEmail() == "Unsigned"){
            navView!!.menu.setGroupVisible(R.id.member,false)
            navView!!.menu.setGroupVisible(R.id.unsigned,true)
            navView
                .getHeaderView(0)
                ?.findViewById<TextView>(R.id.drawer_title)
                ?.text = resources.getString(R.string.initial_user_title)
            findNavController(R.id.nav_host_fragment).graph.startDestination = R.id.nav_login_menu
            findNavController(R.id.nav_host_fragment).navigate(R.id.nav_login_menu)
        }

        setupActionBarWithNavController(navController, drawerLayout)
        navView.setupWithNavController(navController)
    }



    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: ")
        super.onDestroy()
    }



}