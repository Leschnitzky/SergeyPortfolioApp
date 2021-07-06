package com.example.sergeyportfolioapp.usermanagement.ui.profile

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.example.sergeyportfolioapp.R
import com.example.sergeyportfolioapp.UserIntent
import com.example.sergeyportfolioapp.usermanagement.repository.firestore.model.UserForFirestore
import com.example.sergeyportfolioapp.usermanagement.ui.UserViewModel
import com.example.sergeyportfolioapp.usermanagement.ui.profile.state.ProfileViewState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {

    private val userViewModel : UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        setupUI(root)
        return root
    }

    private fun setupUI(root: View?) {
        initializeViews(root)
        observeViewModel()
        setupUser()
        setupClicks()
    }

    private fun setupClicks() {
        editDisplayNameButton.setOnClickListener {
            val alert: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            val edittext = EditText(requireContext())
            alert.setMessage("Choose a new display name")
            alert.setTitle("Display name change")

            alert.setView(edittext)

            alert.setPositiveButton("Apply",
                DialogInterface.OnClickListener { dialog, whichButton -> //What ever you want to do with the value
                    val editTextValue = edittext.text.toString()
                    lifecycleScope.launch {
                        userViewModel.intentChannel.send(UserIntent.UpdateDisplayName(editTextValue))
                    }
                })

            alert.setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, whichButton ->
                    // what ever you want to do with No option.
                })

            alert.show()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                userViewModel.stateProfilePage.collect {
                    when(it) {
                        is ProfileViewState.Idle -> {
                            unlockUI()
                        }
                        is ProfileViewState.Loading -> {
                            lockUI()
                        }

                        is ProfileViewState.LoadedData -> {
                            setupUIWithData(it.userData)
                            unlockUI()
                        }

                    }
                }
            }
        }
    }

    private fun setupUIWithData(userData: UserForFirestore) {
        Glide.with(this)
            .asDrawable()
            .load(userData.profilePicURI)
            .into(profilePic)
        displayNameText.text = userData.displayName
    }

    private fun lockUI() {
        loadingAnimation.visibility = View.VISIBLE
        editDisplayNameButton.isEnabled = false
    }

    private fun unlockUI() {
        loadingAnimation.visibility = View.GONE
        editDisplayNameButton.isEnabled = true

    }

    private fun setupUser() {
        lifecycleScope.launch {
            userViewModel.intentChannel.send(UserIntent.GetProfileData)
        }
    }


    private lateinit var displayNameText : TextView
    private lateinit var editDisplayNameButton : Button
    private lateinit var loadingAnimation : LottieAnimationView
    private lateinit var profilePic : ImageView

    private fun initializeViews(root: View?) {
        displayNameText = root!!.findViewById(R.id.profile_account_display_name)
        editDisplayNameButton = root!!.findViewById(R.id.profile_edit_display_name)
        profilePic = root.findViewById(R.id.profile_profile_pic)
        loadingAnimation = root.findViewById(R.id.profile_loading_animation)
        loadingAnimation.bringToFront()
    }
}
