package com.example.sergeyportfolioapp.util

import android.os.IBinder
import android.view.View
import android.view.WindowManager
import androidx.test.espresso.Root
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher


fun hasTextInputLayoutErrorText(expectedErrorText: String): Matcher<View> = object : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description?) { }

    override fun matchesSafely(item: View?): Boolean {
        if (item !is TextInputLayout) return false
        val error = item.error ?: return false
        val hint = error.toString()
        return expectedErrorText == hint
    }
}

fun isToast(): Matcher<Root?>? {
    return object : TypeSafeMatcher<Root>() {
        override fun describeTo(description: Description) {
            description.appendText("is toast")
        }

        override fun matchesSafely(root: Root): Boolean {
            val type: Int = root.getWindowLayoutParams().get().type
            if (type == WindowManager.LayoutParams.TYPE_TOAST) {
                val windowToken: IBinder = root.getDecorView().getWindowToken()
                val appToken: IBinder = root.getDecorView().getApplicationWindowToken()
                if (windowToken === appToken) {
                    // windowToken == appToken means this window isn't contained by any other windows.
                    // if it was a window for an activity, it would have TYPE_BASE_APPLICATION.
                    return true
                }
            }
            return false
        }
    }
}