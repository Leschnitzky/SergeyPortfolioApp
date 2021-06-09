package com.example.sergeyportfolioapp.usermanagement.ui.login

import android.animation.ValueAnimator
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.example.sergeyportfolioapp.R
import com.example.sergeyportfolioapp.usermanagement.ui.UserViewModel
import com.example.sergeyportfolioapp.usermanagement.ui.login.intent.LoginIntent
import com.example.sergeyportfolioapp.usermanagement.ui.login.viewstate.LoginViewState
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener


@AndroidEntryPoint
class LoginFragment : Fragment(){
    private val TAG = "LoginFragment"
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var loginButton : Button
    private lateinit var passForgetButton : Button
    private lateinit var registerButton : Button
    private lateinit var guideline: Guideline
    private lateinit var emailEditLayout : TextInputLayout
    private lateinit var passwordEditLayout : TextInputLayout
    private lateinit var loadingView : LottieAnimationView;
    private lateinit var greetingAnimation : LottieAnimationView;
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



    private fun setupUI(root: View){
        initiateViewFields(root)
        setKeyboardListener()
        observeViewModel()
        setupClicks()


    }

    private fun initiateViewFields(root: View) {
        greetingAnimation = root.findViewById(R.id.greetingAnimation)
        guideline = root.findViewById(R.id.loginSectionGuideline)
        loginButton = root.findViewById(R.id.button)
        passForgetButton = root.findViewById(R.id.button3)
        registerButton = root.findViewById(R.id.register)
        emailEditLayout = root.findViewById(R.id.emailInput)
        passwordEditLayout = root.findViewById(R.id.passwordInput)
        loadingView = root.findViewById(R.id.animationView)

    }

    private fun setKeyboardListener() {
        KeyboardVisibilityEvent.setEventListener(
            activity as Activity,
            viewLifecycleOwner,
            KeyboardVisibilityEventListener {
                if (it) {
                    val valueAnimator = ValueAnimator.ofFloat(0.5f, 0.05f)
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
                    val valueAnimator = ValueAnimator.ofFloat(0.05f, 0.5f)
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

    private fun observeViewModel() {
        lifecycleScope.launch {
            userViewModel.state.collect {
                when (it) {
                    is LoginViewState.Idle -> {

                    }
                    is LoginViewState.Loading -> {
                        loginButton.isEnabled = false
                        emailEditLayout.isErrorEnabled = true
                        emailEditLayout.isEnabled = false
                        passwordEditLayout.isEnabled = false
                        loadingView.visibility = View.VISIBLE;
                        loadingView.bringToFront();
                    }

                    is LoginViewState.LoggedIn -> {
                        Log.d(TAG, "observeViewModel: Got name")
                        loginButton.isEnabled = true
                        emailEditLayout.isEnabled = true
                        passwordEditLayout.isEnabled = true
                        emailEditLayout.isErrorEnabled = false
                        loadingView.visibility = View.GONE;
                        Toast.makeText(context, String.format(resources.getString(R.string.welcome_message),it.name), Toast.LENGTH_LONG).show()

                    }
                    is LoginViewState.Error -> {
                        loginButton.isEnabled = true
                        emailEditLayout.isEnabled = true
                        passwordEditLayout.isEnabled = true
                        loadingView.visibility = View.GONE;

                        emailEditLayout.error = it.error
                    }
                }
            }
        }
    }


    private fun setupClicks() {
        loginButton.setOnClickListener {
            lifecycleScope.launch {
                userViewModel.userIntent.send(LoginIntent.Login(
                    emailEditLayout.editText?.text.toString(),
                    passwordEditLayout.editText?.text.toString()
                ))
            }
        }
        registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_nav_login_to_registerFragment)
        }

        passForgetButton.setOnClickListener {
            lifecycleScope.launch {
                userViewModel.userIntent.send(LoginIntent.ForgotPass(
                    emailEditLayout.editText?.text.toString()
                ))
            }
        }
    }

}