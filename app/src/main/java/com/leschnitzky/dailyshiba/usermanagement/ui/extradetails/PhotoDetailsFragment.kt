package com.leschnitzky.dailyshiba.usermanagement.ui.extradetails

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.transition.TransitionInflater
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.awesomedialog.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.leschnitzky.dailyshiba.R
import com.leschnitzky.dailyshiba.UserIntent
import com.leschnitzky.dailyshiba.usermanagement.ui.UserViewModel
import com.leschnitzky.dailyshiba.usermanagement.ui.extradetails.state.PhotoDetailsViewState
import com.leschnitzky.dailyshiba.utils.listeners.GeneralTouchListener
import com.leschnitzky.dailyshiba.utils.listeners.OnDoubleClickListener
import com.leschnitzky.dailyshiba.utils.listeners.OnSwipeTouchListener

import com.leschnitzky.dailyshiba.utils.getKey
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File


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
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setTranslationZ(view, 1F)
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

    @ExperimentalCoroutinesApi
    private fun setupUser() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                if(arguments?.getString("uri")?.startsWith("http")!!){
                    userViewModel.intentChannel.send(UserIntent.CheckPhotoInFavorites(
                        arguments?.getString("uri")!!
                    ))
                } else {
                    userViewModel.getCurrentUserURLMap().collect {
                        userViewModel.intentChannel.send(UserIntent.CheckPhotoInFavorites(
                            it[arguments?.getString("uri")!!]!!
                            )
                        )
                    }
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
        dogImage.setOnTouchListener(
            GeneralTouchListener(
            object : OnDoubleClickListener() {
                override fun onSingleClick(v: View?) {

                }

                override fun onDoubleClick(v: View?) {
                    Timber.d("Double CLICK!")
                    lifecycleScope.launch {
                        var uri = arguments?.getString("uri")

                        if(!uri!!.startsWith("http")){
                            userViewModel.getCurrentUserURLMap().collect {
                                uri = it[arguments?.getString("uri")]

                            }
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
            },
            object : OnSwipeTouchListener(requireContext()){
                override fun onSwipeTop() {
                    super.onSwipeTop()
                    val uris = arguments?.getStringArray("uris")
                    val index = uris?.indexOf(arguments?.getString("uri"))
                    Timber.d("$uris")

                    if(index!! + 1 < uris.size - 1){
                        val action = PhotoDetailsFragmentDirections.actionNavDetailsSelfTop(
                            uri = uris?.get(index!! + 1),
                            uris = uris!!
                        )
                        findNavController().navigate(action)
                    }
                }

                override fun onSwipeBottom() {
                    super.onSwipeBottom()
                    val uris = arguments?.getStringArray("uris")
                    Timber.d("$uris")
                    val index = uris?.indexOf(arguments?.getString("uri"))

                    if(index!! - 1 >= 0){
                        val action = PhotoDetailsFragmentDirections.actionNavDetailsSelfBot(
                            uri = uris?.get(index!! - 1),
                            uris = uris!!
                        )
                        findNavController().navigate(action)
                    }
                }
            }
         )
        )

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
                                    userViewModel.getCurrentUserURLMap().collect {
                                        val profilePic = it[arguments?.getString("uri")]

                                        userViewModel.intentChannel.send(
                                            UserIntent.SetProfilePicture(
                                                profilePic!!
                                            )
                                        )
                                    }

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
                    userViewModel.getCurrentUserURLMap().collect {
                        uri = it[arguments?.getString("uri")]
                    }
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

        shareButton.setOnClickListener {
            lockUI()
            lifecycleScope.launch {
                Timber.d("Shared Photo!")
                val photo = arguments?.getString("uri")
                Timber.d("$photo")
                var photoUri: Uri? = null

                if(arguments?.containsKey("intent")!!){
                    Glide.with(requireContext())
                        .asBitmap()
                        .load(photo)
                        .into(object : SimpleTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                Timber.d("Got here!")
                                val bitmapPath = MediaStore
                                    .Images.Media.insertImage(requireActivity().contentResolver, resource, "palette", "share palette");
                                photoUri = Uri.parse(bitmapPath);
                            }
                        });
                    launch {
                        delay(2000)
                    }
                } else {
                    userViewModel.getCurrentUserURLMap().collect {
                        photoUri = if(photo!!.startsWith("http")){
                            FileProvider.getUriForFile(
                                requireContext(),
                                requireActivity().applicationContext.packageName + ".provider",
                                File(getKey(it,photo)))
                        } else {
                            FileProvider.getUriForFile(
                                requireContext(),
                                requireActivity().applicationContext.packageName + ".provider",
                                File(photo));

                        }
                    }
                }
                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.type = "image/jpeg"
                sharingIntent.putExtra(Intent.EXTRA_STREAM, photoUri)
                sharingIntent.putExtra(Intent.EXTRA_TEXT,getString(R.string.daily_shiba_sharing_template))
                resultLauncher.launch(Intent.createChooser(sharingIntent, "Share image using"))
            }
        }


    }

    private lateinit var dogImage : ImageView
    private lateinit var favoriteButton : ToggleButton
    private lateinit var setupAsProfileButton : Button
    private lateinit var shareButton: Button
    private lateinit var loadingAnimation : LottieAnimationView
    private lateinit var adView : AdView

    private fun initializeViews(root: View?) {
        dogImage = root!!.findViewById(R.id.drawer_profile_pic)
        setupAsProfileButton = root!!.findViewById(R.id.details_profile_button)
        favoriteButton = root!!.findViewById(R.id.details_set_as_favorite)
        favoriteButton.textOn = "";
        favoriteButton.textOff = "";
        shareButton = root.findViewById(R.id.details_share_button)
        loadingAnimation = root!!.findViewById(R.id.details_loading_animation)
        loadingAnimation.bringToFront()

        adView = root.findViewById(R.id.details_ad_view)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }


    private fun unlockUI() {
        favoriteButton.isEnabled = true
        setupAsProfileButton.isEnabled = true
        loadingAnimation.visibility = View.GONE

        shareButton.isEnabled = true
        activity?.window?.clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun lockUI() {
        shareButton.isEnabled = false
        favoriteButton.isEnabled = false
        setupAsProfileButton.isEnabled = false
        loadingAnimation.visibility = View.VISIBLE
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )

    }


}