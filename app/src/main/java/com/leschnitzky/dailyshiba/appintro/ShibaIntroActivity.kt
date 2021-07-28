package com.leschnitzky.dailyshiba.appintro

import android.Manifest
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import com.leschnitzky.dailyshiba.R

class ShibaIntroActivity : AppIntro() {
    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        askForPermissions(
            permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE),
            slideNumber = 3,
            required = true)
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR;

        addSlide(
            AppIntroFragment.newInstance(
            title = "Welcome to Daily Shiba!", titleColor = ContextCompat.getColor(this, R.color.design_default_color_primary),
            backgroundDrawable = R.color.design_default_color_secondary ,
            imageDrawable = R.drawable.ic_app_icon,
            description = "A free and easy to use Dog Browser that lets you easily browse and share dog photos" , descriptionColor = ContextCompat.getColor(this,R.color.design_default_color_primary_variant)
        ))
        addSlide(AppIntroFragment.newInstance(
            title = "Choose your favorite breed!", titleColor = ContextCompat.getColor(this, R.color.design_default_color_primary),
            backgroundDrawable = R.color.design_default_color_secondary ,
            imageDrawable = R.drawable.ic_shiba,
            description = "Each member can choose an assortment of dog breeds to view inside the picture browser" , descriptionColor = ContextCompat.getColor(this,R.color.design_default_color_primary_variant)
        ))

        addSlide(AppIntroFragment.newInstance(
            title = "Like photos!", titleColor = ContextCompat.getColor(this, R.color.design_default_color_primary),
            backgroundDrawable = R.color.design_default_color_secondary ,
            imageDrawable = R.drawable.ic_favorite,
            description = "You can like photos and keep them in your account to access them from your phone anytime you like!" , descriptionColor = ContextCompat.getColor(this,R.color.design_default_color_primary_variant)
        ))

        addSlide(AppIntroFragment.newInstance(
            title = "Share Photos!", titleColor = ContextCompat.getColor(this, R.color.design_default_color_primary),
            backgroundDrawable = R.color.design_default_color_secondary ,
            imageDrawable = R.drawable.ic_intro_share,
            description = "Share photos with your friends on Facebook, Whatsapp and more!" , descriptionColor = ContextCompat.getColor(this,R.color.design_default_color_primary_variant)
        ))

        addSlide(AppIntroFragment.newInstance(
            title = "Let's get started!", titleColor = ContextCompat.getColor(this, R.color.design_default_color_primary),
            backgroundDrawable = R.color.design_default_color_secondary,
            description = "That's all for now, enjoy the app!" , descriptionColor = ContextCompat.getColor(this,R.color.design_default_color_primary_variant)
        ))
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        finish()
    }


    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        finish()
    }
}