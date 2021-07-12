package com.example.sergeyportfolioapp.usermanagement.ui.extradetails

import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.awesomedialog.*
import com.example.sergeyportfolioapp.R
import com.example.sergeyportfolioapp.UserIntent
import com.example.sergeyportfolioapp.usermanagement.ui.UserViewModel
import com.example.sergeyportfolioapp.usermanagement.ui.extradetails.state.PhotoDetailsViewState
import com.google.firebase.firestore.auth.User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@AndroidEntryPoint
class PhotoDetailsFragment : Fragment() {
    private val TAG = "PhotoDetailsFragment"
    private val userViewModel : UserViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(R.transition.move)
        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(R.transition.move)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uri = arguments?.getString("uri")
        view.findViewById<ImageView>(R.id.drawer_profile_pic).apply {
                transitionName = uri
                Glide.with(this)
                    .load(uri!!)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .centerCrop()
                    .into(this)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root =  inflater.inflate(R.layout.fragment_photo_details, container, false)
        setupUI(root)
        return root
    }

    private fun setupUI(root: View?) {
        initializeViews(root)
        setupUIObserver()
        setupClicks()
        setupUser()
    }

    private fun setupUser() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                if(arguments?.getString("uri")?.startsWith("http")!!){
                    userViewModel.intentChannel.send(UserIntent.CheckPhotoInFavorites(
                        arguments?.getString("uri")!!
                    ))
                } else {
                    userViewModel.intentChannel.send(UserIntent.CheckPhotoInFavorites(
                        userViewModel.getCurrentUserURLMap()[arguments?.getString("uri")!!]!!
                    ))
                }
            }
        }
    }

    private fun setupUIObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                userViewModel.stateDetailsPage.collect {
                    Log.d(TAG, "setupUIObserver: Got $it")

                    when(it){
                        is PhotoDetailsViewState.Idle -> {
                            unlockUI()
                            favoriteButton.isChecked = false
                        }
                        is PhotoDetailsViewState.Loading -> {
                            lockUI()
                        }

                        is PhotoDetailsViewState.PictureIsFavorite -> {
                            unlockUI()
                            favoriteButton.isChecked = true
                        }
                    }
                }
            }
        }
    }

    private fun setupClicks() {
        dogImage.setOnClickListener {
            findNavController().popBackStack()
        }
        setupAsProfileButton.setOnClickListener {
            lifecycleScope.launch {
                AwesomeDialog
                    .build(requireActivity())
                    .title(
                        titleColor = ContextCompat.getColor(requireContext(),R.color.design_default_color_primary),
                        title = resources.getString(R.string.shiba_profile_request_title)
                    )
                    .body(resources.getString(R.string.shiba_profile_request_body))
                    .onPositive(
                        buttonBackgroundColor = R.color.design_default_color_secondary,
                        text = resources.getString(R.string.shiba_profile_request_accept),
                        action = {
                            // Show ad and load more photos
                            lifecycleScope.launch {
                                val uri = arguments?.getString("uri")!!

                                if(uri.startsWith("http")){
                                    userViewModel.intentChannel.send(
                                        UserIntent.SetProfilePicture(
                                            uri
                                        )
                                    )
                                } else {
                                    Log.d(TAG, "setupClicks: ${arguments?.getString("uri")}")
                                    Log.d(TAG, "setupClicks: Current Map: ${userViewModel.getCurrentUserURLMap()}")
                                    val profilePic =
                                        userViewModel.getCurrentUserURLMap()[arguments?.getString("uri")]

                                    userViewModel.intentChannel.send(
                                        UserIntent.SetProfilePicture(
                                            profilePic!!
                                        )
                                    )
                                }
                            }
                        }
                    )
                    .onNegative(
                        buttonBackgroundColor = R.color.design_default_color_secondary,
                        text = resources.getString(R.string.shiba_profile_request_decline),
                        action = {
                        }
                    )

            }
        }

        favoriteButton.setOnClickListener {
            lifecycleScope.launch {
                var uri = arguments?.getString("uri")

                if(!uri!!.startsWith("http")){
                    uri = userViewModel.getCurrentUserURLMap()[arguments?.getString("uri")]
                }

                Log.d(TAG, "setupClicks: $uri")

                if(favoriteButton.isChecked) {
                    userViewModel.intentChannel.send(
                        UserIntent.AddPictureFavorite(
                            uri!!
                        )
                    )
                } else {
                    userViewModel.intentChannel.send(
                        UserIntent.RemovePictureFavorite(
                            uri!!
                        )
                    )
                }
            }
        }
    }

    private lateinit var dogImage : ImageView
    private lateinit var favoriteButton : ToggleButton
    private lateinit var setupAsProfileButton : Button
    private lateinit var loadingAnimation : LottieAnimationView

    private fun initializeViews(root: View?) {
        dogImage = root!!.findViewById(R.id.drawer_profile_pic)
        setupAsProfileButton = root!!.findViewById(R.id.details_profile_button)
        favoriteButton = root!!.findViewById(R.id.details_set_as_favorite)
        loadingAnimation = root!!.findViewById(R.id.details_loading_animation)
        loadingAnimation.bringToFront()
    }


    private fun unlockUI() {
        favoriteButton.isEnabled = true
        setupAsProfileButton.isEnabled = true
        loadingAnimation.visibility = View.GONE

        activity?.window?.clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun lockUI() {
        favoriteButton.isEnabled = false
        setupAsProfileButton.isEnabled = false
        loadingAnimation.visibility = View.VISIBLE
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )

    }


}