package com.example.sergeyportfolioapp.shibaphotodisplay.ui.main

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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
import com.example.sergeyportfolioapp.shibaphotodisplay.ui.PhotoIntent
import com.example.sergeyportfolioapp.shibaphotodisplay.ui.PhotoViewModel
import com.example.sergeyportfolioapp.shibaphotodisplay.ui.main.state.ShibaViewState
import com.example.sergeyportfolioapp.usermanagement.ui.UserViewModel
import com.example.sergeyportfolioapp.utils.FOLDER_NAME
import com.example.sergeyportfolioapp.utils.RecycleViewScrollDisabler
import com.example.sergeyportfolioapp.utils.getInternalFileOutstream
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class ShibaFragment : Fragment() {

    private val ALREADY_GOT_PHOTOS: Int = 0
    private val GET_NEW_PHOTOS: Int = 10
    private lateinit var welcomeText: TextView
    private lateinit var localPhotoPaths : ArrayList<String>
    private lateinit var loadingAnimation : LottieAnimationView
    private lateinit var recyclerView: RecyclerView
    private lateinit var job: Job
    private lateinit var getMorePhotosButton : Button
    var disabler: OnItemTouchListener = RecycleViewScrollDisabler()
    private val photoViewModel: PhotoViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val TAG = "ShibaFragment"


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition()
    }
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
        setupClicks()
        getInitialPhotos()

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
                             photoViewModel.userIntent.send(
                                 PhotoIntent.MorePhotos
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

    private fun observeViewModel() {
        lifecycleScope.launch {
            photoViewModel.photoLoadState.collect {
                when(it) {
                    is ShibaViewState.Idle -> {
                        unlockUI()
                    }
                    is ShibaViewState.Loading -> {
                        lockUI()
                    }

                    is ShibaViewState.GotPhotos -> {
                        Log.d(TAG, "observeViewModel: Got photos")
                        updateRecyclerViewWithList(it.list,1)
                        CoroutineScope(Dispatchers.IO).launch {
                             savePhotosLocally(it.list).also {
                                 list ->
                                     Log.d(TAG, "observeViewModel: $list")
                                     userViewModel.updatePhotosToCurrentUserDB(list)
                            }.also {
                                 withContext(Dispatchers.Main){
                                     unlockUI()

                                 }
                             }
                        }
                        
                    }

                    is ShibaViewState.Error -> {
                        unlockUI()

                        Toast.makeText(context,it.error,Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            userViewModel.currPhotosInDB.collect {
                Log.d(TAG, "observeViewModel: Got new photos from DB ")
                when(it) {
                    is ShibaViewState.GotPhotos -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            loadPhotosToRecyclerViewFromLocalStorage(
                                it.list.filter { string ->  string != "" } as ArrayList<String>
                            ).let {
                                withContext(Dispatchers.Main){
                                    unlockUI()
                                }
                            }
                        }
                    }

                    is ShibaViewState.Loading ->{
                        lockUI()
                    }

                    is ShibaViewState.Error -> {
                        photoViewModel.getNewPhotosFromServer()
                    }
                }
            }
        }
    }

    private fun lockUI() {
        recyclerView.addOnItemTouchListener(disabler)
        loadingAnimation.visibility = View.VISIBLE
        getMorePhotosButton.isEnabled = false
    }

    private fun unlockUI() {
        recyclerView.removeOnItemTouchListener(disabler)
        recyclerView.isHorizontalScrollBarEnabled = true
        getMorePhotosButton.isEnabled = true
        loadingAnimation.visibility = View.GONE
    }

    private suspend fun loadPhotosToRecyclerViewFromLocalStorage(arrayList: ArrayList<String>) {
        Log.d(TAG, "loadPhotosToRecyclerViewFromLocalStorage: ")
        withContext(Dispatchers.Main){
            updateRecyclerViewWithList(arrayList,0)
        }
    }

    private suspend fun savePhotosLocally(list: List<String>): ArrayList<String> {
        localPhotoPaths = arrayListOf()
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
                            continuation.resume(resource){}
                            val filename = "${userViewModel.getCurrentUserEmail()}_${index}.png"
                            resource.compress(
                                Bitmap.CompressFormat.PNG,
                                0,
                                getInternalFileOutstream(
                                    requireContext(),
                                    filename
                                )
                            )
                            val fullpath = "${requireContext().externalCacheDir}/$FOLDER_NAME/$filename"
                            Log.d(TAG, "onResourceReady: $fullpath")
                            if(!localPhotoPaths.contains(fullpath)){
                                localPhotoPaths.add(fullpath)
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }
                    })
            }

        }
        return localPhotoPaths

    }

    private fun updateRecyclerViewWithList(list: List<String>, mode: Int) {
        Log.d(TAG, "updateRecyclerViewWithList: Got new update for recycler")
        val recyclerViewAdapter = RecyclerViewAdapter(list,mode,requireContext(),userViewModel)
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

    private fun getInitialPhotos() {
        Log.d(TAG, "getInitialPhotos: Called initial Photos")
        userViewModel.getPhotosFromDB()
    }

    private fun initializeViews(root: View?) {
        getMorePhotosButton = root!!.findViewById(R.id.get_more_photos_button)
        welcomeText = root!!.findViewById(R.id.shiba_welcome)
        welcomeText.text = String.format(resources.getString(R.string.welcoming_shiba),arguments?.getString("name"))
        loadingAnimation = root!!.findViewById(R.id.shiba_loading_animation)
        loadingAnimation.bringToFront()
        recyclerView = root!!.findViewById(R.id.shiba_recycler_view)


    }

    override fun onDestroy() {
        super.onDestroy()
    }


}