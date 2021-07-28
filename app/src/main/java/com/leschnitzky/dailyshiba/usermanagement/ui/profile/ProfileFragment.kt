package com.leschnitzky.dailyshiba.usermanagement.ui.profile

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.leschnitzky.dailyshiba.R
import com.leschnitzky.dailyshiba.UserIntent
import com.leschnitzky.dailyshiba.usermanagement.repository.firestore.model.UserForFirestore
import com.leschnitzky.dailyshiba.usermanagement.ui.UserViewModel
import com.leschnitzky.dailyshiba.usermanagement.ui.profile.state.ProfileViewState
import com.leschnitzky.dailyshiba.utils.getStringBooleanRepr
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

        toggleShiba.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                userViewModel.intentChannel.send(UserIntent.UpdateUserSettings(
                    "dogs",
                    "s",
                    getStringBooleanRepr(isChecked)
                ))
            }
        }
        toggleBeagle.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                userViewModel.intentChannel.send(UserIntent.UpdateUserSettings(
                    "dogs",
                    "b",
                    getStringBooleanRepr(isChecked)
                ))
            }
        }
        toggleHusky.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                userViewModel.intentChannel.send(UserIntent.UpdateUserSettings(
                    "dogs",
                    "h",
                    getStringBooleanRepr(isChecked)
                ))
            }
        }
        toggleCorgi.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                userViewModel.intentChannel.send(UserIntent.UpdateUserSettings(
                    "dogs",
                    "c",
                    getStringBooleanRepr(isChecked)
                ))
            }
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

                        is ProfileViewState.Error -> {
                            setupUser()
                            Toast.makeText(requireContext(), it.error, Toast.LENGTH_SHORT).show()
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
        updateToggleButtons(userData.userSettings.settings["dogs"])
    }

    private fun updateToggleButtons(dogString: String?) {
        toggleShiba.isChecked = isShibaToggleEnabled(dogString)
        toggleHusky.isChecked = isHuskyToggleEnabled(dogString)
        toggleCorgi.isChecked = isCorgiToggleEnabled(dogString)
        toggleBeagle.isChecked = isBeagleToggleEnabled(dogString)
    }

    private fun isBeagleToggleEnabled(dogString: String?): Boolean {
        return isToggleEnabled(dogString,"b")
    }

    private fun isCorgiToggleEnabled(dogString: String?): Boolean {
        return isToggleEnabled(dogString, "c")
    }

    private fun isHuskyToggleEnabled(dogString: String?): Boolean {
        return isToggleEnabled(dogString, "h")
    }

    private fun isShibaToggleEnabled(dogString: String?): Boolean {
        return isToggleEnabled(dogString,"s")
    }

    private fun isToggleEnabled(dogString: String?, s: String) : Boolean {
        return dogString?.get((dogString?.indexOf(s) + 1)) == '1'
    }

    private fun lockUI() {
        loadingAnimation.visibility = View.VISIBLE
        editDisplayNameButton.isEnabled = false
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        toggleBeagle.isEnabled = false
        toggleCorgi.isEnabled = false
        toggleHusky.isEnabled = false
        toggleShiba.isEnabled = false
    }

    private fun unlockUI() {
        loadingAnimation.visibility = View.GONE
        activity?.window?.clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        editDisplayNameButton.isEnabled = true
        toggleBeagle.isEnabled = true
        toggleCorgi.isEnabled = true
        toggleHusky.isEnabled = true
        toggleShiba.isEnabled = true

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
    private lateinit var toggleShiba : ToggleButton
    private lateinit var toggleCorgi : ToggleButton
    private lateinit var toggleHusky : ToggleButton
    private lateinit var toggleBeagle : ToggleButton

    private fun initializeViews(root: View?) {
        displayNameText = root!!.findViewById(R.id.profile_account_display_name)
        editDisplayNameButton = root!!.findViewById(R.id.profile_edit_display_name)
        profilePic = root.findViewById(R.id.profile_profile_pic)
        loadingAnimation = root.findViewById(R.id.profile_loading_animation)
        loadingAnimation.bringToFront()
        toggleShiba = root.findViewById(R.id.profile_shiba_toggle)
        toggleCorgi = root.findViewById(R.id.profile_corgi_toggle)
        toggleHusky = root.findViewById(R.id.profile_husky_toggle)
        toggleBeagle = root.findViewById(R.id.profile_beagle_toggle)
    }
}
