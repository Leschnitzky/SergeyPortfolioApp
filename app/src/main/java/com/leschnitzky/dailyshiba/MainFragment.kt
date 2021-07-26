package com.leschnitzky.dailyshiba

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.*
import com.leschnitzky.dailyshiba.usermanagement.ui.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
    private val userViewModel: UserViewModel by activityViewModels()


    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE), 1)
        val root = inflater.inflate(R.layout.fragment_main, container, false)

        lifecycleScope.launch() {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                checkUserConnection()
            }
        }
        return root
    }



    private suspend fun checkUserConnection(){
        userViewModel.intentChannel.send(UserIntent.CheckLogin)
    }


    private lateinit var savedStateHandle: SavedStateHandle

}