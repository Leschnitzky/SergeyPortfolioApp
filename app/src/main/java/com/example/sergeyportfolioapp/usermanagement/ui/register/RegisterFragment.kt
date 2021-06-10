package com.example.sergeyportfolioapp.usermanagement.ui.register

import android.animation.ValueAnimator
import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.example.sergeyportfolioapp.R
import com.example.sergeyportfolioapp.usermanagement.ui.UserIntent
import com.example.sergeyportfolioapp.usermanagement.ui.UserViewModel
import com.example.sergeyportfolioapp.usermanagement.ui.register.viewstate.RegisterViewState
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener

@AndroidEntryPoint
class RegisterFragment : Fragment() {


    private val userViewModel: UserViewModel by viewModels()
    private lateinit var nameInputLayout: TextInputLayout
    private lateinit var greetingText : TextView
    private lateinit var emailSubText : TextView
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var loadingView : LottieAnimationView
    private lateinit var registerButton : Button
    private lateinit var facebookButton : ImageButton
    private lateinit var googleButton : ImageButton
    private lateinit var guideline: Guideline
    private lateinit var radioBox : RadioButton





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_register, container, false)
        setupUI(root!!);
        return root;
    }

    private fun setupUI(root: View) {
        initiateViewFields(root)
        setKeyboardListener()
        observeViewModel()
        setupIntents()
    }

    private fun setupIntents() {
        registerButton.setOnClickListener {
            lifecycleScope.launch {
                userViewModel.userIntent.send(
                    UserIntent.Register(nameInputLayout.editText?.text.toString(),
                    emailInputLayout.editText?.text.toString(),
                    passwordInputLayout.editText?.text.toString(),
                        radioBox.isChecked
                ))
            }
        }
    }

    private fun observeViewModel() {
            lifecycleScope.launch {
                userViewModel.stateRegisterPage.collect {
                    when (it) {
                        is RegisterViewState.Idle -> {

                        }
                        is RegisterViewState.Loading -> {
                            googleButton.isEnabled = false;
                            facebookButton.isEnabled = false;

                            nameInputLayout.isErrorEnabled = true
                            emailInputLayout.isErrorEnabled = true
                            passwordInputLayout.isErrorEnabled = true

                            emailInputLayout.isEnabled = false
                            passwordInputLayout.isEnabled = false
                            nameInputLayout.isEnabled = false;
                            loadingView.visibility = View.VISIBLE;
                            loadingView.bringToFront();
                        }

                        is RegisterViewState.Registered -> {
                            registerButton.isEnabled = true
                            emailInputLayout.isEnabled = true
                            passwordInputLayout.isEnabled = true
                            nameInputLayout.isEnabled = true

                            nameInputLayout.isErrorEnabled = false
                            emailInputLayout.isErrorEnabled = false
                            passwordInputLayout.isErrorEnabled = false
                            loadingView.visibility = View.GONE;
                            Toast.makeText(context, String.format(resources.getString(R.string.welcome_message),it.name), Toast.LENGTH_LONG).show()

                        }
                        is RegisterViewState.Error -> {
                            registerButton.isEnabled = true
                            emailInputLayout.isEnabled = true
                            passwordInputLayout.isEnabled = true
                            nameInputLayout.isEnabled = true
                            loadingView.visibility = View.GONE;

                            when(it.error_code.value){
                                RegisterViewState.RegisterErrorCode.EMPTY_EMAIL.value -> {
                                    emailInputLayout.error = it.error
                                    passwordInputLayout.isErrorEnabled = false
                                    nameInputLayout.isErrorEnabled = false

                                }
                                RegisterViewState.RegisterErrorCode.EMPTY_PASSWORD.value -> {
                                    passwordInputLayout.error = it.error
                                    nameInputLayout.isErrorEnabled = false
                                    emailInputLayout.isErrorEnabled = false

                                }
                                RegisterViewState.RegisterErrorCode.EMPTY_NAME.value -> {
                                    nameInputLayout.error = it.error
                                    passwordInputLayout.isErrorEnabled = false
                                    emailInputLayout.isErrorEnabled = false

                                }
                                RegisterViewState.RegisterErrorCode.INVALID_EMAIL.value -> {
                                    emailInputLayout.error = it.error
                                    passwordInputLayout.isErrorEnabled = false
                                    nameInputLayout.isErrorEnabled = false

                                }
                                RegisterViewState.RegisterErrorCode.FIREBASE_ERROR.value -> {
                                    nameInputLayout.error = it.error
                                    passwordInputLayout.isErrorEnabled = false
                                    emailInputLayout.isErrorEnabled = false

                                }
                                RegisterViewState.RegisterErrorCode.DIDNT_ACCEPT_TERMS.value -> {
                                    nameInputLayout.error = it.error
                                    passwordInputLayout.isErrorEnabled = false
                                    emailInputLayout.isErrorEnabled = false
                                }

                            }
                        }
                    }
                }
            }
    }

    private fun setKeyboardListener() {
            KeyboardVisibilityEvent.setEventListener(
                activity as Activity,
                viewLifecycleOwner,
                KeyboardVisibilityEventListener {
                    if (it) {
                        emailSubText.visibility = View.GONE;
                        greetingText.visibility = View.GONE
                        val valueAnimator = ValueAnimator.ofFloat(0.35f, 0.05f)
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
                        emailSubText.visibility = View.VISIBLE;
                        greetingText.visibility = View.VISIBLE
                        val valueAnimator = ValueAnimator.ofFloat(0.05f, 0.35f)
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

    private fun initiateViewFields(root: View) {
        googleButton = root.findViewById(R.id.google_sign_up)
        facebookButton = root.findViewById(R.id.facebook_sign_up)
        radioBox = root.findViewById(R.id.radioButton)
        emailSubText = root.findViewById(R.id.email_subtext)
        greetingText = root.findViewById(R.id.register_greeting)
        registerButton = root.findViewById(R.id.register_done)
        nameInputLayout = root.findViewById(R.id.register_page_name_layout)
        emailInputLayout = root.findViewById(R.id.register_page_email_layout)
        passwordInputLayout = root.findViewById(R.id.register_page_password_layout)
        guideline = root.findViewById(R.id.register_page_accountdetails_seperator)
        loadingView = root.findViewById(R.id.register_page_loading_animation)

    }


}