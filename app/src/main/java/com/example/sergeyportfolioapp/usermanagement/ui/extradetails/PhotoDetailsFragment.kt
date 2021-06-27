package com.example.sergeyportfolioapp.usermanagement.ui.extradetails

import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.awesomedialog.*
import com.example.sergeyportfolioapp.R
import com.example.sergeyportfolioapp.usermanagement.ui.UserIntent
import com.example.sergeyportfolioapp.usermanagement.ui.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class PhotoDetailsFragment : Fragment() {
    private val userViewModel : UserViewModel by viewModels()
    private lateinit var dogImage : ImageView
    private lateinit var favoriteButton : ToggleButton
    private lateinit var setupAsProfileButton : Button

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
        setupClicks()
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
                                userViewModel._intentChannel.send(
                                        UserIntent.SetProfilePicture(
                                            userViewModel.getCurrentUserURLMap()[arguments?.getString("uri")]!!
                                        )
                                )

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
    }

    private fun initializeViews(root: View?) {
        dogImage = root!!.findViewById(R.id.drawer_profile_pic)
        setupAsProfileButton = root!!.findViewById(R.id.details_profile_button)
        favoriteButton = root!!.findViewById(R.id.details_set_as_favorite)
    }

}