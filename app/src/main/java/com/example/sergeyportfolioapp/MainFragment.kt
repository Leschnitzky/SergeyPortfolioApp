package com.example.sergeyportfolioapp

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.fragment.findNavController
import com.example.sergeyportfolioapp.usermanagement.ui.UserViewModel
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint


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
            findNavController().navigate(R.id.action_nav_main_to_nav_shiba)
        }
        val root =  inflater.inflate(R.layout.fragment_main, container, false)
        return root
    }

    private lateinit var savedStateHandle: SavedStateHandle


}