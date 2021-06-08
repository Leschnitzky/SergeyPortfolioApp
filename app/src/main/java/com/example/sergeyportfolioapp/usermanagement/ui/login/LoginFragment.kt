package com.example.sergeyportfolioapp.usermanagement.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.example.sergeyportfolioapp.R
import com.example.sergeyportfolioapp.usermanagement.ui.UserViewModel
import com.example.sergeyportfolioapp.usermanagement.ui.login.intent.LoginIntent
import com.example.sergeyportfolioapp.usermanagement.ui.login.viewstate.LoginViewState
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private val TAG = "LoginFragment"
    private val homeViewModel: UserViewModel by viewModels()
    private lateinit var button : Button
    private lateinit var emailEditLayout : TextInputLayout
    private lateinit var passwordEditLayout : TextInputLayout
    private lateinit var loadingView : LottieAnimationView;
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        setupUI(root)
        observeViewModel()
        setupClicks()
        return root
    }

    private fun setupUI(root: View){
        button = root.findViewById(R.id.button)
        emailEditLayout = root.findViewById(R.id.emailInput)
        passwordEditLayout = root.findViewById(R.id.passwordInput)
        loadingView = root.findViewById(R.id.animationView)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            homeViewModel.state.collect {
                when (it) {
                    is LoginViewState.Idle -> {

                    }
                    is LoginViewState.Loading -> {
                        button.isEnabled = false
                        emailEditLayout.isErrorEnabled = true
                        emailEditLayout.isEnabled = false
                        passwordEditLayout.isEnabled = false
                        loadingView.visibility = View.VISIBLE;
                    }

                    is LoginViewState.LoggedIn -> {
                        Log.d(TAG, "observeViewModel: Got name")
                        button.isEnabled = true
                        emailEditLayout.isEnabled = true
                        passwordEditLayout.isEnabled = true
                        emailEditLayout.isErrorEnabled = false
                        loadingView.visibility = View.GONE;
                        Toast.makeText(context, String.format(resources.getString(R.string.welcome_message),it.name), Toast.LENGTH_LONG).show()

                    }
                    is LoginViewState.Error -> {
                        button.isEnabled = true
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
        button.setOnClickListener {
            lifecycleScope.launch {
                homeViewModel.userIntent.send(LoginIntent.Login(
                    emailEditLayout.editText?.text.toString(),
                    passwordEditLayout.editText?.text.toString()
                ))
            }
        }
    }
}