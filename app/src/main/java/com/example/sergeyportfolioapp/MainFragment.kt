package com.example.sergeyportfolioapp

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.fragment.findNavController
import com.example.sergeyportfolioapp.usermanagement.ui.UserViewModel
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainFragment : Fragment() {

    private val userViewModel: UserViewModel by viewModels()
    private val TAG = "MainFragment"
    private lateinit var navView : NavigationView;

    var LOGIN_SUCCESSFUL: String = "LOGIN_SUCCESSFUL"
    


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: ")
        LOGIN_SUCCESSFUL = userViewModel.getCurrentUserEmail()

        if(LOGIN_SUCCESSFUL == "Unsigned") {
            findNavController().navigate(R.id.action_nav_main_to_nav_login2);

        } else {
            MainScope().launch {
                userViewModel.getUserDisplayName().let {
                    findNavController().navigate(
                        R.id.nav_shiba,
                        bundleOf("name" to it)
                    )
                }
            }
        }
        val root =  inflater.inflate(R.layout.fragment_main, container, false)
        requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE), 1)
        return root
    }

    private lateinit var savedStateHandle: SavedStateHandle


}