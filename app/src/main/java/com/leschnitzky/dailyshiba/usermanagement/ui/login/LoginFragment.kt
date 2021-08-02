package com.leschnitzky.dailyshiba.usermanagement.ui.login

import android.animation.ValueAnimator
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.leschnitzky.dailyshiba.R
import com.leschnitzky.dailyshiba.UserIntent
import com.leschnitzky.dailyshiba.usermanagement.ui.UserViewModel
import com.leschnitzky.dailyshiba.usermanagement.ui.login.viewstate.LoginViewState
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.textfield.TextInputLayout
import com.leschnitzky.dailyshiba.utils.isConnected
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import timber.log.Timber
import java.io.InputStream
import javax.inject.Inject


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LoginFragment : Fragment(){
    private val TAG = "LoginFragment"
    val userViewModel: UserViewModel by activityViewModels()

    private lateinit var loginButton : Button
    @Inject lateinit var mGoogleSignInClient: GoogleSignInClient
    @Inject lateinit var mCallbackManager: CallbackManager
    @Inject lateinit var mLoginManager: LoginManager
    private lateinit var termsAndConditionsButton : Button
    private lateinit var registerButton : Button
    private lateinit var guideline: Guideline
    private lateinit var emailEditLayout : TextInputLayout
    private lateinit var passwordEditLayout : TextInputLayout
    private lateinit var loadingView : LottieAnimationView;
    private lateinit var googleButton : ImageButton
    private lateinit var facebookButton : ImageButton
    private lateinit var greetingAnimation : LottieAnimationView;
    private lateinit var forgotPassButton : Button
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
        Timber.d( "$result: ")
        if (result.resultCode == RESULT_OK) {
            Timber.d( "Got result: ")
            // There are no request codes
            val data: Intent? = result.data

            lifecycleScope.launch {
                Timber.d("launching Intent: ")
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
        termsAndConditionsButton = root.findViewById(R.id.terms_and_conds_button)
        registerButton = root.findViewById(R.id.register)
        emailEditLayout = root.findViewById(R.id.emailInput)
        passwordEditLayout = root.findViewById(R.id.passwordInput)
        loadingView = root.findViewById(R.id.animationView)
        loadingView.bringToFront()
        forgotPassButton = root.findViewById(R.id.login_forgot_password)

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
                Timber.d( "observeViewModel: Started")
                observeLoginViewState()
            }
        }
    }

    @InternalCoroutinesApi
    private suspend fun observeLoginViewState() {
            userViewModel.stateLoginPage.collect {
                Timber.d( "observeLoginViewState: Got $it")
                when (it) {
                    is LoginViewState.Idle -> {
                        unlockUI()
                    }
                    is LoginViewState.Loading -> {
                        lockUI()

                    }
                    is LoginViewState.ResetEmailSent -> {
                        unlockUI()

                        Toast.makeText(requireContext(),getString(R.string.email_sent_confirmation), Toast.LENGTH_SHORT).show()
                    }

                    is LoginViewState.LoggedIn -> {
                        unlockUI()

                    }
                    is LoginViewState.Error -> {
                        unlockUI()
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

    private fun unlockUI(){
        activity?.window?.clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        loginButton.isEnabled = true
        emailEditLayout.isEnabled = true
        passwordEditLayout.isEnabled = true
        facebookButton.isEnabled = true
        registerButton.isEnabled = true
        termsAndConditionsButton.isEnabled = true
        googleButton.isEnabled = true
        loadingView.visibility = View.GONE;
        forgotPassButton.isEnabled = true

    }

    private fun lockUI() {
        loginButton.isEnabled = false
        facebookButton.isEnabled = false
        googleButton.isEnabled = false

        emailEditLayout.isErrorEnabled = true
        passwordEditLayout.isErrorEnabled = true
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        emailEditLayout.isEnabled = false
        passwordEditLayout.isEnabled = false
        registerButton.isEnabled = false
        termsAndConditionsButton.isEnabled = false
        forgotPassButton.isEnabled = false
        loadingView.visibility = View.VISIBLE;    }


    private fun setupClicks() {
        mLoginManager.registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Timber.d( "facebook:onSuccess:$loginResult")

                lifecycleScope.launch {
                    userViewModel.intentChannel.send(UserIntent.FacebookSignIn(loginResult.accessToken))
                }
            }

            override fun onCancel() {
                Timber.d( "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                Timber.d( "facebook:onError $error")
            }
        })

        loginButton.setOnClickListener {
            if(isConnected(requireContext())){
                viewLifecycleOwner.lifecycleScope.launch {
                    userViewModel.intentChannel.send(
                        UserIntent.Login(
                            emailEditLayout.editText?.text.toString(),
                            passwordEditLayout.editText?.text.toString()
                        ))
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.no_internet_connection),
                    Toast.LENGTH_SHORT).show()
            }
        }
        registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_nav_login_to_registerFragment)
        }

        termsAndConditionsButton.setOnClickListener {
            createTermsAndCondsDialog()
        }

        forgotPassButton.setOnClickListener {
            lifecycleScope.launch {
                userViewModel.intentChannel.send(
                    UserIntent.SendResetPassEmail(
                        emailEditLayout.editText?.text.toString()
                    )
                )
            }
        }

        googleButton.setOnClickListener {
            Timber.d( "setupClicks: Launching intent")
            val intent = mGoogleSignInClient.signInIntent
            resultLauncher.launch(intent)
        }

        facebookButton.setOnClickListener {
            Timber.d( "setupClicks: Facebook button")
            mLoginManager.logInWithReadPermissions(this,listOf("email", "public_profile"))
        }


    }

    private fun createTermsAndCondsDialog() {
        val inflater = LayoutInflater.from(requireContext())
        val view: View = inflater.inflate(R.layout.scrollable_dialog_view, null)

        val textview = view.findViewById<View>(R.id.textmsg) as TextView
        try {
            val res: Resources = resources
            val in_s: InputStream = res.openRawResource(R.raw.terms_and_cond)
            val b = ByteArray(in_s.available())
            in_s.read(b)
            textview.text = String(b)
        } catch (e: Exception) {
            textview.text = "Error: can't show terms."
        }
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        alertDialog.setTitle("Terms and Conditions")
        alertDialog.setView(view)
        alertDialog.setPositiveButton("OK", null)
        val alert: AlertDialog = alertDialog.create()
        alert.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d( "onActivityResult: ")
        super.onActivityResult(requestCode, resultCode, data)
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }


}