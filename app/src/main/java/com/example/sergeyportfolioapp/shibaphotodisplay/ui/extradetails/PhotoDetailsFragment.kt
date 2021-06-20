package com.example.sergeyportfolioapp.shibaphotodisplay.ui.extradetails

import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ToggleButton
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.sergeyportfolioapp.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PhotoDetailsFragment : Fragment() {
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
        view.findViewById<ImageView>(R.id.profile_pic).apply {
                transitionName = uri
                Glide.with(this)
                    .load(uri!!)
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
    }

    private fun initializeViews(root: View?) {
        dogImage = root!!.findViewById(R.id.profile_pic)
        setupAsProfileButton = root!!.findViewById(R.id.details_profile_button)
        favoriteButton = root!!.findViewById(R.id.details_set_as_favorite)
    }

}