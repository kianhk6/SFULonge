package com.example.sfulounge

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.example.sfulounge.ui.login.LoginActivity
import com.example.sfulounge.ui.personality.PersonalityTest
import com.example.sfulounge.ui.profile.ProfileFragment
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersonalityActivityLaunchTest {

    // this has to be the launcher activity
    @get:Rule
    val activityRule = ActivityTestRule(LoginActivity::class.java)

    // this assumes that your account is set up and you are logged out of your account
    @Test
    fun testPersonalityButtonClick() {

        // simulate a successful login
//        onView(withId(R.id.email)).perform(typeText("divij.gupta1001@gmail.com"))
//        onView(withId(R.id.password)).perform(typeText("divij.gupta1001@gmail.com"))
//        onView(withId(R.id.login)).perform(click())

//        Thread.sleep(3000)
        // now the app should navigate to MainActivity
//        intended(hasComponent(MainActivity::class.java.name))

        // Launch the ProfileFragment within the MainActivity
//        val scenario = launchFragmentInContainer<ProfileFragment>()

        // click the button in the fragment
//        onView(withId(R.id.personality)).perform(click())

        // check if the correct activity is launched
//        intended(hasComponent(PersonalityTest::class.java.name))
    }

    @Test
    fun mainActivityTest() {
        // Wait for LoginActivity to automatically log in and transition to MainActivity
//        Thread.sleep(3000) // 3 seconds delay

        // Now MainActivity should be visible
//        onView(withId(R.id.someViewInMainActivity)).check(matches(isDisplayed()))
        // ... perform other actions and assertions
    }
}