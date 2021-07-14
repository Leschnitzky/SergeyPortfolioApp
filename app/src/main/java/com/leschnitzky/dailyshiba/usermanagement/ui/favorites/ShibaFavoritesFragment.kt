package com.leschnitzky.dailyshiba.usermanagement.ui.favorites

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.leschnitzky.dailyshiba.R
import com.leschnitzky.dailyshiba.UserIntent
import com.leschnitzky.dailyshiba.usermanagement.ui.RecyclerViewAdapter
import com.leschnitzky.dailyshiba.usermanagement.ui.UserViewModel
import com.leschnitzky.dailyshiba.usermanagement.ui.favorites.state.ShibaFavoritesStateView
import com.leschnitzky.dailyshiba.utils.RecycleViewScrollDisabler
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class ShibaFavoritesFragment : Fragment() {
    private val TAG = "ShibaFavoritesFragment"
    val userViewModel : UserViewModel by activityViewModels()

    var disabler: RecyclerView.OnItemTouchListener = RecycleViewScrollDisabler()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root =  inflater.inflate(R.layout.fragment_shiba_favorites, container, false)
        setupUI(root)
        return root
    }

    private fun setupUI(root: View?) {
        initializeViews(root)
        observeViewModel()
        setupUser()
    }

    private fun setupUser() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                userViewModel.intentChannel.send(UserIntent.UpdateFavoritesPage)
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                userViewModel.stateFavoritePage.collect {
                    when(it){
                        is ShibaFavoritesStateView.Idle -> {
                            unlockUI()
                        }
                        is ShibaFavoritesStateView.InitState -> {}
                        is ShibaFavoritesStateView.Loading -> {
                            lockUI()
                        }
                        is ShibaFavoritesStateView.PhotosLoaded -> {
                            initRecyclerView(it.list)
                            unlockUI()
                        }
                    }
                }
            }
        }
    }

    private fun initRecyclerView(list: List<String>) {
        Timber.d( "initRecyclerView: $list")
        val adapter = RecyclerViewAdapter(list,0,requireContext(),userViewModel)
        adapter.photoSelectedListener = object : RecyclerViewAdapter.PhotoSelectedListener {
            override fun onPhotoSelected(imageView: ImageView, uri: String) {

                Timber.d( "onPhotoSelected: $uri")
                val extras = FragmentNavigatorExtras(
                    imageView to uri
                )

                val action = ShibaFavoritesFragmentDirections.actionNavFavoritesToNavDetails(uri = uri)
                findNavController().navigate(action, extras)
            }
        }

        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager
        recyclerView.doOnPreDraw {
            startPostponedEnterTransition()
        }

    }

    private fun lockUI() {
        recyclerView.addOnItemTouchListener(disabler)
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        loadingAnimation.visibility = View.VISIBLE
    }

    private fun unlockUI() {
        recyclerView.removeOnItemTouchListener(disabler)
        activity?.window?.clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        loadingAnimation.visibility = View.GONE
    }

    private lateinit var loadingAnimation : LottieAnimationView
    private lateinit var recyclerView : RecyclerView
    private lateinit var adView : AdView

    private fun initializeViews(root: View?) {
        recyclerView = root!!.findViewById(R.id.favorite_recycler_view)
        loadingAnimation = root.findViewById(R.id.favorites_loading_animation)
        loadingAnimation.bringToFront()

        adView = root.findViewById(R.id.favorites_ad_view)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

}