package com.example.sergeyportfolioapp

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.sergeyportfolioapp.usermanagement.ui.UserIntent
import com.example.sergeyportfolioapp.usermanagement.ui.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*


@AndroidEntryPoint
class MainFragment : Fragment() {

    private val userViewModel: UserViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setupUI()
        requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE), 1)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun setupUI() {
        initializeUser()
    }

    private fun initializeUser() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                userViewModel._intentChannel.send(UserIntent.CheckLogin)
            }
        }
    }



    private lateinit var savedStateHandle: SavedStateHandle


}