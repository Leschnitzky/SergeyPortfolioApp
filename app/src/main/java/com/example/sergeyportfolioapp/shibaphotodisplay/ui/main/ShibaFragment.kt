package com.example.sergeyportfolioapp.shibaphotodisplay.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.sergeyportfolioapp.R
import com.example.sergeyportfolioapp.shibaphotodisplay.ui.PhotoIntent
import com.example.sergeyportfolioapp.shibaphotodisplay.ui.PhotoViewModel
import com.example.sergeyportfolioapp.shibaphotodisplay.ui.main.state.ShibaViewState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ShibaFragment : Fragment() {

    private lateinit var welcomeText: TextView
    private lateinit var loadingAnimation : LottieAnimationView
    private lateinit var recyclerView: RecyclerView
    private val photoViewModel: PhotoViewModel by viewModels()
    private val TAG = "ShibaFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_shiba, container, false)

        setupUI(root)
        return root
    }

    private fun setupUI(root: View?) {
        initializeViews(root)
        observeViewModel()
        getInitialPhotos()
        setupClicks()

    }

    private fun setupClicks() {

    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            photoViewModel.photoLoadState.collect {
                when(it) {
                    is ShibaViewState.Idle -> {

                    }
                    is ShibaViewState.Loading -> {
                        loadingAnimation.visibility = View.VISIBLE
                    }

                    is ShibaViewState.GotPhotos -> {
                        loadingAnimation.visibility = View.GONE
                        val recyclerViewAdapter = RecyclerViewAdapter(it.list,requireContext())
                        val layoutManager = LinearLayoutManager(requireContext())
                        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
                        recyclerView.adapter = recyclerViewAdapter
                        recyclerView.layoutManager = layoutManager
                        Log.d(TAG, "observeViewModel: ${it.list}")
                    }

                    is ShibaViewState.Error -> {
                        loadingAnimation.visibility = View.GONE

                        Toast.makeText(context,it.error,Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun getInitialPhotos() {
        lifecycleScope.launch {
            photoViewModel.userIntent.send(PhotoIntent.MorePhotos)
        }
    }

    private fun initializeViews(root: View?) {
        welcomeText = root!!.findViewById(R.id.shiba_welcome)
        welcomeText.text = String.format(resources.getString(R.string.welcoming_shiba),arguments?.getString("name"))
        loadingAnimation = root!!.findViewById(R.id.shiba_loading_animation)
        recyclerView = root!!.findViewById(R.id.shiba_recycler_view)


    }


}