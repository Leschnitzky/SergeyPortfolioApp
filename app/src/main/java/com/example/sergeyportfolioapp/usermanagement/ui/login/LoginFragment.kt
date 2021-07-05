package com.example.sergeyportfolioapp.usermanagement.ui.login

import android.animation.ValueAnimator
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.example.sergeyportfolioapp.R
import com.example.sergeyportfolioapp.usermanagement.ui.UserViewModel
import com.example.sergeyportfolioapp.UserIntent
import com.example.sergeyportfolioapp.usermanagement.ui.login.viewstate.LoginViewState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import javax.inject.Inject


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LoginFragment : Fragment(){
    private val TAG = "LoginFragment"
    private val userViewModel: UserViewModel by activityViewModels()

    private lateinit var loginButton : Button
    @Inject lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var passForgetButton : Button
    private lateinit var registerButton : Button
    private lateinit var guideline: Guideline
    private lateinit var emailEditLayout : TextInputLayout
    private lateinit var passwordEditLayout : TextInputLayout
    private lateinit var loadingView : LottieAnimationView;
    private lateinit var googleButton : ImageButton
    private lateinit var facebookButton : ImageButton
    private lateinit var greetingAnimation : LottieAnimationView;
    @InternalCoroutinesApi
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        val root = inflater.inflate(R.layout.fragment_login, container, false)
        setupUI(root)
        return root
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d(TAG, "$result: ")
        if (result.resultCode == RESULT_OK) {
            Log.d(TAG, "Got result: ")
            // There are no request codes
            val data: Intent? = result.data

            lifecycleScope.launch {
                Log.d(TAG, "launching Intent: ")
                userViewModel.intentChannel.send(
                    UserIntent.SignInGoogle(
                        GoogleSignIn.getSignedInAccountFromIntent(data)
                    )
                )

            }
        }
    }



    @InternalCoroutinesApi
    private fun setupUI(root: View){
        initiateViewFields(root)
        setKeyboardListener()
        observeViewModel()
        setupClicks()
    }

    private fun initiateViewFields(root: View) {
        greetingAnimation = root.findViewById(R.id.greetingAnimation)
        guideline = root.findViewById(R.id.loginSectionGuideline)
        googleButton = root.findViewById(R.id.google_sign_in)
        facebookButton = root.findViewById(R.id.facebook_sign_in)
        loginButton = root.findViewById(R.id.button)
        passForgetButton = root.findViewById(R.id.button3)
        registerButton = root.findViewById(R.id.register)
        emailEditLayout = root.findViewById(R.id.emailInput)
        passwordEditLayout = root.findViewById(R.id.passwordInput)
        loadingView = root.findViewById(R.id.animationView)
        loadingView.bringToFront()

    }

    private fun setKeyboardListener() {
        KeyboardVisibilityEvent.setEventListener(
            activity as Activity,
            viewLifecycleOwner,
            KeyboardVisibilityEventListener {
                if (it) {
                    val valueAnimator = ValueAnimator.ofFloat(0.5f, 0.02f)
                    valueAnimator.duration = 250
                    // set duration
                    valueAnimator.interpolator = AccelerateDecelerateInterpolator()
                    // set interpolator and  updateListener to get the animated value
                    valueAnimator.addUpdateListener { valueAnimator ->
                        val lp = guideline.layoutParams as ConstraintLayout.LayoutParams
                        // get the float value
                        lp.guidePercent = valueAnimator.animatedValue as Float
                        // update layout params
                        guideline.layoutParams = lp
                    }
                    valueAnimator.start()
                } else {
                    val valueAnimator = ValueAnimator.ofFloat(0.02f, 0.5f)
                    valueAnimator.duration = 250
                    // set duration
                    valueAnimator.interpolator = AccelerateDecelerateInterpolator()
                    // set interpolator and  updateListener to get the animated value
                    valueAnimator.addUpdateListener { valueAnimator ->
                        val lp = guideline.layoutParams as ConstraintLayout.LayoutParams
                        // get the float value
                        lp.guidePercent = valueAnimator.animatedValue as Float
                        // update layout params
                        guideline.layoutParams = lp
                    }
                    valueAnimator.start()
                }
            })
    }

    @InternalCoroutinesApi
    private fun observeViewModel() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                observeLoginViewState()
            }
        }
    }

    @InternalCoroutinesApi
    private suspend fun observeLoginViewState() {
            userViewModel.stateLoginPage.collectLatest {
                when (it) {
                    is LoginViewState.Idle -> {

                    }
                    is LoginViewState.Loading -> {
                        lockUI()

                    }

                    is LoginViewState.LoggedIn -> {
                        Log.d(TAG, "observeViewModel: Got name")
                        loginButton.isEnabled = true
                        emailEditLayout.isEnabled = true
                        passwordEditLayout.isEnabled = true

                        emailEditLayout.isErrorEnabled = false
                        passwordEditLayout.isErrorEnabled = false

                        loadingView.visibility = View.GONE;

                    }
                    is LoginViewState.Error -> {
                        loginButton.isEnabled = true
                        emailEditLayout.isEnabled = true
                        passwordEditLayout.isEnabled = true
                        loadingView.visibility = View.GONE;

                        when(it.error_code.value){
                            LoginViewState.LoginErrorCode.EMPTY_EMAIL.value -> {
                                emailEditLayout.error = it.error
                                passwordEditLayout.isErrorEnabled = false;
                            }
                            LoginViewState.LoginErrorCode.EMPTY_PASSWORD.value -> {
                                emailEditLayout.isErrorEnabled = false;
                                passwordEditLayout.error = it.error
                            }
                            LoginViewState.LoginErrorCode.INVALID_EMAIL.value -> {
                                emailEditLayout.error = it.error
                                passwordEditLayout.isErrorEnabled = false;
                            }
                            LoginViewState.LoginErrorCode.FIREBASE_ERROR.value -> {
                                emailEditLayout.error = it.error
                                passwordEditLayout.isErrorEnabled = false;

                            }


                        }
                    }
                }
            }
    }

    private fun lockUI() {
        loginButton.isEnabled = false

        emailEditLayout.isErrorEnabled = true
        passwordEditLayout.isErrorEnabled = true

        emailEditLayout.isEnabled = false
        passwordEditLayout.isEnabled = false
        registerButton.isEnabled = false
        passForgetButton.isEnabled = false
        loadingView.visibility = View.VISIBLE;    }


    private fun setupClicks() {
        loginButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                userViewModel.intentChannel.send(
                    UserIntent.Login(
                    emailEditLayout.editText?.text.toString(),
                    passwordEditLayout.editText?.text.toString()
                ))
            }
        }
        registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_nav_login_to_registerFragment)
        }

        passForgetButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                userViewModel.intentChannel.send(
                    UserIntent.ForgotPass(
                    emailEditLayout.editText?.text.toString()
                ))
            }
        }


        googleButton.setOnClickListener {
            Log.d(TAG, "setupClicks: Launching intent")
            val intent = mGoogleSignInClient.signInIntent
            resultLauncher.launch(intent)
        }
    }

}