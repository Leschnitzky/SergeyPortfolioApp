package com.leschnitzky.dailyshiba.usermanagement.ui.main

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.awesomedialog.*
import com.leschnitzky.dailyshiba.R
import com.leschnitzky.dailyshiba.usermanagement.ui.main.state.ShibaViewState
import com.leschnitzky.dailyshiba.UserIntent
import com.leschnitzky.dailyshiba.usermanagement.ui.RecyclerViewAdapter
import com.leschnitzky.dailyshiba.usermanagement.ui.UserViewModel
import com.leschnitzky.dailyshiba.utils.FOLDER_NAME
import com.leschnitzky.dailyshiba.utils.RecycleViewScrollDisabler
import com.leschnitzky.dailyshiba.utils.getInternalFileOutstream
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import timber.log.Timber


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ShibaFragment : Fragment() {

    private lateinit var welcomeText: TextView
    private lateinit var localPhotoPaths : ArrayList<String>
    private lateinit var loadingAnimation : LottieAnimationView
    private lateinit var recyclerView: RecyclerView
    private lateinit var getMorePhotosButton : Button
    private lateinit var adView : AdView
    private var mInterstitialAd: InterstitialAd? = null

    var disabler: OnItemTouchListener = RecycleViewScrollDisabler()
    private val userViewModel: UserViewModel by activityViewModels()

    private val TAG = "ShibaFragment"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_shiba, container, false)

        setupUI(root)
        Timber.d( "onCreateView: returned root")
        return root
    }

    private fun setupUI(root: View?) {
        initializeViews(root)
        observeViewModel()
        setupClicks()
        setupUser()
    }
    private fun initializeViews(root: View?) {
        getMorePhotosButton = root!!.findViewById(R.id.get_more_photos_button)
        welcomeText = root!!.findViewById(R.id.shiba_welcome)
        welcomeText.text = String.format(
            resources.getString(
                R.string.welcoming_shiba)
                ,requireActivity().findViewById<TextView>(
                    R.id.drawer_title
                ).text.toString()
        )
        loadingAnimation = root!!.findViewById(R.id.shiba_loading_animation)
        loadingAnimation.bringToFront()

        adView = root.findViewById(R.id.shiba_ad_view)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        recyclerView = root!!.findViewById(R.id.shiba_recycler_view)

        loadAd(adRequest)


    }

    private fun loadAd(adRequest: AdRequest) {
        InterstitialAd.load(requireContext(), getString(R.string.admob_id), adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Timber.d( "SERG " + adError?.message)
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Timber.d( "SERG Ad was loaded.")
                mInterstitialAd = interstitialAd
            }
        })

        mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Timber.d( "SERG Ad was dismissed.")
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                Timber.d( "SERG Ad failed to show.")
            }

            override fun onAdShowedFullScreenContent() {
                Timber.d( "SERG Ad showed fullscreen content.")
                mInterstitialAd = null;
            }
        }
    }


    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                    observeShibaViewState()
            }
    }

    }


    private fun setupClicks() {
        getMorePhotosButton.setOnClickListener {
            AwesomeDialog
                .build(requireActivity())
                .title(
                    titleColor = ContextCompat.getColor(requireContext(),R.color.design_default_color_primary),
                    title = resources.getString(R.string.shiba_more_photos_dialog_title)
                )
                .body(resources.getString(R.string.shiba_more_photos_dialog_body))
                .onPositive(
                    buttonBackgroundColor = R.color.design_default_color_secondary,
                    text = resources.getString(R.string.shiba_more_photos_dialog_button_text),
                    action = {
                        // Show ad and load more photos
                        if (mInterstitialAd != null) {
                            mInterstitialAd?.show(requireActivity())
                        } else {
                            Log.d("TAG", "The interstitial ad wasn't ready yet.")
                        }

                        lifecycleScope.launch {
                            userViewModel.intentChannel.send(
                                UserIntent.GetNewPhotos
                            )
                        }
                        val adRequest = AdRequest.Builder().build()
                        loadAd(adRequest)
                    }
                )
                .onNegative(
                    buttonBackgroundColor = R.color.design_default_color_secondary,

                    text = resources.getString(R.string.shiba_more_photos_dialog_button_negative_text),
                    action = {
                    }
                )
        }
    }


    private fun setupUser() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                getInitialPhotos()
            }
        }
    }


    private suspend fun observeShibaViewState() {
            userViewModel.stateShibaPage.collect {
                Timber.d( "observeViewModel: Got ShibaViewState $it ")
                when(it) {
                    is ShibaViewState.GotPhotos -> {
                        val original = it.list.subList(1,it.list.size)
                        if(it.list.first() == "SaveLocally"){
                            savePhotosLocally(original).also { localPhotoList ->
                                Timber.d( "Got Local Photos: Next action")
                                userViewModel.updatePhotosToCurrentUserDB(localPhotoList,original)
                            }.also {
                                updateRecyclerViewWithList(original,0)
                            }.apply {
                                unlockUI()
                            }
                        } else {
                            updateRecyclerViewWithList(it.list,0).also {
                                unlockUI()
                            }
                        }
                    }
                    is ShibaViewState.Loading ->{
                        lockUI()
                    }

                    is ShibaViewState.Error -> {
                        displayError(it.error)
                        unlockUI()
                    }
                    is ShibaViewState.Idle -> {
                        unlockUI()
                    }
                }
            }
    }

    private fun displayError(error: String) {
        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
    }


    private fun lockUI() {
        recyclerView.addOnItemTouchListener(disabler)
        loadingAnimation.visibility = View.VISIBLE
        getMorePhotosButton.isEnabled = false

        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)


    }

    private fun unlockUI() {
        recyclerView.removeOnItemTouchListener(disabler)
        getMorePhotosButton.isEnabled = true
        loadingAnimation.visibility = View.GONE

        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)


    }


    private suspend fun savePhotosLocally(list: List<String>): ArrayList<String> {
        localPhotoPaths = arrayListOf()
        var shouldStop = false;
        list.forEachIndexed {
            index, url ->
             suspendCancellableCoroutine<Bitmap> {
                continuation ->
                 continuation.invokeOnCancellation {
                     throw Exception("Cancelled")
                 }
                Timber.d( "savePhotosLocally: Starting Glide: With URL : $url and Index: $index")
                Glide
                    .with(requireContext())
                    .asBitmap()
                    .load(url)
                    .override(250,250)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                                val filename = "${userViewModel.getCurrentUserEmail()}_${index}.png"
                                resource.compress(
                                    Bitmap.CompressFormat.PNG,
                                    0,
                                    getInternalFileOutstream(
                                        requireContext(),
                                        filename
                                    )
                                )
                                val fullpath =
                                    "${requireContext().externalCacheDir}/$FOLDER_NAME/$filename"
                                Timber.d( "onResourceReady: $fullpath")
                                if (!localPhotoPaths.contains(fullpath)) {
                                    localPhotoPaths.add(fullpath)
                                }
                                if (index == 9) {
                                    shouldStop = true
                                }
                            continuation.resume(resource) {}
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }

                    })
            }
        }
        return localPhotoPaths

    }

    private fun updateRecyclerViewWithList(list: List<String>, mode: Int) {
        Timber.d( "updateRecyclerViewWithList: Got new update for recycler $list")
            val recyclerViewAdapter = RecyclerViewAdapter(list.dropLast(1),mode,requireContext(),userViewModel)
            recyclerViewAdapter.photoSelectedListener = object : RecyclerViewAdapter.PhotoSelectedListener {
                override fun onPhotoSelected(imageView: ImageView, uri: String, position: Int) {
                    val extras = FragmentNavigatorExtras(
                        imageView to uri
                    )
                    userViewModel.currentPosition = position;
                    val action = ShibaFragmentDirections.actionNavShibaToPhotoDetailsFragment(uri = uri)
                    findNavController().navigate(action, extras)
                }
            }
            val layoutManager = LinearLayoutManager(requireContext())
            val gridLayoutManager = GridLayoutManager(requireContext(),3)
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL
            recyclerView.adapter = recyclerViewAdapter
            recyclerView.layoutManager = gridLayoutManager
            recyclerView.doOnPreDraw {
                startPostponedEnterTransition()
            }
            recyclerView.scrollToPosition(userViewModel.currentPosition);
    }

    private suspend fun getInitialPhotos() {
        userViewModel.intentChannel.send(UserIntent.GetPhotos)
    }









}