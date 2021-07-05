package com.example.sergeyportfolioapp.usermanagement.ui.main

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.awesomedialog.*
import com.example.sergeyportfolioapp.R
import com.example.sergeyportfolioapp.usermanagement.ui.main.state.ShibaViewState
import com.example.sergeyportfolioapp.UserIntent
import com.example.sergeyportfolioapp.usermanagement.ui.RecyclerViewAdapter
import com.example.sergeyportfolioapp.usermanagement.ui.UserViewModel
import com.example.sergeyportfolioapp.utils.FOLDER_NAME
import com.example.sergeyportfolioapp.utils.RecycleViewScrollDisabler
import com.example.sergeyportfolioapp.utils.getInternalFileOutstream
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ShibaFragment : Fragment() {

    private lateinit var welcomeText: TextView
    private lateinit var localPhotoPaths : ArrayList<String>
    private lateinit var loadingAnimation : LottieAnimationView
    private lateinit var recyclerView: RecyclerView
    private lateinit var getMorePhotosButton : Button
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
        Log.d(TAG, "onCreateView: returned root")
        return root
    }

    private fun setupUI(root: View?) {
        Log.d(TAG, "setupUI: 1")
        initializeViews(root)
        Log.d(TAG, "setupUI: 2")
        observeViewModel()
        Log.d(TAG, "setupUI: 3")
        setupClicks()
        Log.d(TAG, "setupUI: 4")
        setupUser()
        Log.d(TAG, "setupUI: 5")


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
        recyclerView = root!!.findViewById(R.id.shiba_recycler_view)
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
                        lifecycleScope.launch {
                            userViewModel.intentChannel.send(
                                UserIntent.GetNewPhotos
                            )
                        }
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
                Log.d(TAG, "observeViewModel: Got ShibaViewState $it ")
                when(it) {
                    is ShibaViewState.GotPhotos -> {
                        val original = it.list.subList(1,it.list.size)
                        if(it.list.first() == "SaveLocally"){
                            savePhotosLocally(original).also { localPhotoList ->
                                Log.d(TAG, "Got Local Photos: Next action")
                                userViewModel.updatePhotosToCurrentUserDB(localPhotoList,original)
                            }.also {
                                updateRecyclerViewWithList(original,0)
                            }.apply {
                                unlockUI()
                            }
                        } else {
                            updateRecyclerViewWithList(it.list,0).apply {
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
                    is ShibaViewState.Idle -> {}
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
        activity?.findViewById<DrawerLayout>(R.id.drawer_layout)?.isFocusable = false
    }

    private fun unlockUI() {
        recyclerView.removeOnItemTouchListener(disabler)
        getMorePhotosButton.isEnabled = true
        loadingAnimation.visibility = View.GONE
        activity?.findViewById<DrawerLayout>(R.id.drawer_layout)?.isFocusable = true

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
                Log.d(TAG, "savePhotosLocally: Starting Glide: With URL : $url and Index: $index")
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
                                Log.d(TAG, "onResourceReady: $fullpath")
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
        Log.d(TAG, "updateRecyclerViewWithList: Got new update for recycler $list")
            val recyclerViewAdapter = RecyclerViewAdapter(list.dropLast(1),mode,requireContext(),userViewModel)
            recyclerViewAdapter.photoSelectedListener = object : RecyclerViewAdapter.PhotoSelectedListener {
                override fun onPhotoSelected(imageView: ImageView, uri: String) {
                    val extras = FragmentNavigatorExtras(
                        imageView to uri
                    )

                    val action = ShibaFragmentDirections.actionNavShibaToPhotoDetailsFragment(uri = uri)
                    findNavController().navigate(action, extras)
                }
            }
            val layoutManager = LinearLayoutManager(requireContext())
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL
            recyclerView.adapter = recyclerViewAdapter
            recyclerView.layoutManager = layoutManager
            recyclerView.doOnPreDraw {
                startPostponedEnterTransition()
        }
    }

    private suspend fun getInitialPhotos() {
        userViewModel.intentChannel.send(UserIntent.GetPhotos)
    }






}