package com.example.sergeyportfolioapp.usermanagement.ui

import android.R
import android.app.Activity
import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ResourcesProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getString(@StringRes stringResId: Int): String {
        return context.getString(stringResId)
    }

}