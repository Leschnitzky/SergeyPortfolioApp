package com.leschnitzky.dailyshiba.usermanagement.ui.register

import android.animation.ValueAnimator
import android.app.Activity
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.textfield.TextInputLayout
import com.leschnitzky.dailyshiba.R
import com.leschnitzky.dailyshiba.UserIntent
import com.leschnitzky.dailyshiba.usermanagement.ui.UserViewModel
import com.leschnitzky.dailyshiba.usermanagement.ui.register.viewstate.RegisterViewState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import timber.log.Timber
import java.io.InputStream


@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private val TAG = "RegisterFragment"


    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var nameInputLayout: TextInputLayout
    private lateinit var greetingText : TextView
    private lateinit var emailSubText : TextView
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var loadingView : LottieAnimationView
    private lateinit var registerButton : Button
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

    private fun createFragmentListener() {
        parentFragmentManager.addFragmentOnAttachListener { _, fragment ->
            if(fragment is RegisterFragment) {
                Timber.d( "createFragmentListener: ")

            }
        }
    }

    private fun setupIntents() {
        registerButton.setOnClickListener {
            lifecycleScope.launch {
                userViewModel.intentChannel.send(
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
                repeatOnLifecycle(Lifecycle.State.STARTED){
                    userViewModel.stateRegisterPage.collect {
                        handleViewState(it)
                    }
                }
            }
    }

    private fun handleViewState(it: RegisterViewState) {
        when (it) {
            is RegisterViewState.Idle -> {

            }
            is RegisterViewState.Loading -> {

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
        radioBox = root.findViewById(R.id.radioButton)
        initRadioBox();
        emailSubText = root.findViewById(R.id.email_subtext)
        greetingText = root.findViewById(R.id.register_greeting)
        registerButton = root.findViewById(R.id.register_done)
        nameInputLayout = root.findViewById(R.id.register_page_name_layout)
        emailInputLayout = root.findViewById(R.id.register_page_email_layout)
        passwordInputLayout = root.findViewById(R.id.register_page_password_layout)
        guideline = root.findViewById(R.id.register_page_thirdparty_seperator)
        loadingView = root.findViewById(R.id.register_page_loading_animation)
        loadingView.bringToFront()

    }

    private fun initRadioBox() {
        val linkClick: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                createTermsAndCondsDialog()
                view.invalidate()
            }

            override fun updateDrawState(ds: TextPaint) {
                if (radioBox.isPressed) {
                    ds.color = ContextCompat.getColor(requireContext(), R.color.design_default_color_primary)
                } else {
                    ds.color = ContextCompat.getColor(requireContext(), R.color.design_default_color_secondary)
                }
                radioBox.invalidate()
            }
        }
        radioBox.highlightColor = Color.TRANSPARENT
        val spannableString: Spannable = SpannableString(getString(R.string.terms_and_conditions_text))
        spannableString.setSpan(linkClick, 15, 35, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        radioBox.setText(spannableString, TextView.BufferType.SPANNABLE)
        radioBox.movementMethod = LinkMovementMethod.getInstance()
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
        alertDialog.setPositiveButton("OK", DialogInterface.OnClickListener {
                dialog, which ->
            radioBox.isChecked = true;
        })
        val alert: AlertDialog = alertDialog.create()
        alert.show()
    }


}