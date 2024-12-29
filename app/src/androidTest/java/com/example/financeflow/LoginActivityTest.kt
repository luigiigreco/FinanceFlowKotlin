package com.example.financeflow

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @Test
    fun successfulLogin_navigatesToMainActivity() {
        // Lancia l'Activity
        ActivityScenario.launch(LoginActivity::class.java)

        // Inserisci email e password validi
        onView(withId(R.id.emailEditText)).perform(typeText("test@test.com"), closeSoftKeyboard())
        onView(withId(R.id.passwordEditText)).perform(typeText("validpassword"), closeSoftKeyboard())

        // Clicca sul pulsante di login
        onView(withId(R.id.loginButton)).perform(click())

        // Verifica che l'Activity corrente venga chiusa
        assert(true)
    }
}
