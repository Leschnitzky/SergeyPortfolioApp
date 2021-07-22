package com.leschnitzky.dailyshiba.usermanagement.ui.extradetails

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.awesomedialog.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.leschnitzky.dailyshiba.R
import com.leschnitzky.dailyshiba.UserIntent
import com.leschnitzky.dailyshiba.usermanagement.ui.UserViewModel
import com.leschnitzky.dailyshiba.usermanagement.ui.extradetails.state.PhotoDetailsViewState
import com.leschnitzky.dailyshiba.utils.OnDoubleClickListener
import com.leschnitzky.dailyshiba.utils.getKey
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream


@AndroidEntryPoint
class PhotoDetailsFragment : Fragment() {
    private val TAG = "PhotoDetailsFragment"
    private val userViewModel : UserViewModel by activityViewModels()

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Timber.d( "$result: ")
        unlockUI()
        if (result.resultCode == Activity.RESULT_OK) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(R.transition.move)
        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(R.transition.move)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.photo_details_app_bar_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.details_share_photo -> {
                lockUI()
                lifecycleScope.launch {
                    Timber.d("Shared Photo!")
                    val photo = arguments?.getString("uri")
                    Timber.d("$photo")
                    var photoUri: Uri? = null
                    photoUri = if(photo!!.startsWith("http")){
                        FileProvider.getUriForFile(
                            requireContext(),
                            requireActivity().applicationContext.packageName + ".provider",
                            File(getKey(userViewModel.getCurrentUserURLMap(),photo)))
                    } else {
                        FileProvider.getUriForFile(
                            requireContext(),
                            requireActivity().applicationContext.packageName + ".provider",
                            File(photo));

                    }
                    val sharingIntent = Intent(Intent.ACTION_SEND)
                    try {
                        val stream: InputStream? = requireActivity().contentResolver.openInputStream(photoUri)
                    } catch (e: FileNotFoundException) {
                        Timber.e(e)
                        e.printStackTrace()
                    }
                    sharingIntent.type = "image/jpeg"
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, photoUri)
                    sharingIntent.putExtra(Intent.EXTRA_TEXT,getString(R.string.daily_shiba_sharing_template))
                    resultLauncher.launch(Intent.createChooser(sharingIntent, "Share image using"))
                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
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
                    Timber.d( "setupUIObserver: Got $it")

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
        dogImage.setOnClickListener(object : OnDoubleClickListener() {
            override fun onSingleClick(v: View?) {

            }

            override fun onDoubleClick(v: View?) {
                Timber.d("Double CLICK!")
                lifecycleScope.launch {
                    var uri = arguments?.getString("uri")

                    if(!uri!!.startsWith("http")){
                        uri = userViewModel.getCurrentUserURLMap()[arguments?.getString("uri")]
                    }
                    favoriteButton.isChecked = !favoriteButton.isChecked
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

        })
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
                                    Timber.d( "setupClicks: ${arguments?.getString("uri")}")
                                    Timber.d( "setupClicks: Current Map: ${userViewModel.getCurrentUserURLMap()}")
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

                Timber.d( "setupClicks: $uri")

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
        favoriteButton.textOn = "";
        favoriteButton.textOff = "";
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