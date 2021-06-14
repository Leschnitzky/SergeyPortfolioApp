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
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.airbnb.lottie.LottieAnimationView
import com.example.sergeyportfolioapp.R
import com.example.sergeyportfolioapp.usermanagement.ui.UserViewModel
import com.example.sergeyportfolioapp.usermanagement.ui.UserIntent
import com.example.sergeyportfolioapp.usermanagement.ui.UserTitleState
import com.example.sergeyportfolioapp.usermanagement.ui.login.viewstate.LoginViewState
import com.example.sergeyportfolioapp.usermanagement.ui.register.viewstate.RegisterViewState
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
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
        createFragmentListener()
        initiateViewFields(root)
        setKeyboardListener()
        observeViewModel()
        setupClicks()


    }

    private fun createFragmentListener() {
        parentFragmentManager.addFragmentOnAttachListener { _, fragment ->
                if(fragment is LoginFragment) {
                    Log.d(TAG, "createFragmentListener: ")

                    userViewModel.currentFragmentNumber =
                        UserViewModel.FragmentDisplayNumber.LoginFragment.number
                }
        }
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

        lifecycleScope.launchWhenStarted {
            userViewModel.userTitle.collect {
                val navView = activity?.findViewById<NavigationView>(R.id.nav_view)
                Log.d(TAG, "onCreate: Got $it")
                when(it){
                    is UserTitleState.Member -> {

                        navView
                            ?.getHeaderView(0)
                            ?.findViewById<TextView>(R.id.drawer_title)
                            ?.text = it.name

                        navView!!.menu.setGroupVisible(R.id.member,true)
                        navView!!.menu.setGroupVisible(R.id.unsigned,false)
                        activity?.findNavController(R.id.nav_host_fragment)?.graph?.startDestination = R.id.nav_shiba
                        activity?.findNavController(R.id.nav_host_fragment)?.navigate(R.id.nav_shiba)


                    }
                    is UserTitleState.Guest -> {
                        navView
                            ?.getHeaderView(0)
                            ?.findViewById<TextView>(R.id.drawer_title)
                            ?.text = resources.getString(R.string.initial_user_title)

                        navView!!.menu.setGroupVisible(R.id.unsigned,true)
                        navView!!.menu.setGroupVisible(R.id.member,false)
                    }
                    is UserTitleState.InitState -> {}
                }


            }
        }
        lifecycleScope.launch {
            userViewModel.stateLoginPage.collect {
                when (it) {
                    is LoginViewState.Idle -> {

                    }
                    is LoginViewState.Loading -> {
                        loginButton.isEnabled = false

                        emailEditLayout.isErrorEnabled = true
                        passwordEditLayout.isErrorEnabled = true

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
    }


    private fun setupClicks() {
        loginButton.setOnClickListener {
            lifecycleScope.launch {
                userViewModel.userIntent.send(
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
            lifecycleScope.launch {
                userViewModel.userIntent.send(
                    UserIntent.ForgotPass(
                    emailEditLayout.editText?.text.toString()
                ))
            }
        }
    }

}