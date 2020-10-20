package com.wong.letsdiscusspsb;


import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class Login {

    @Rule
    public ActivityTestRule<SplashScreen> mActivityTestRule = new ActivityTestRule<>(SplashScreen.class);

    @Test
    public void login() {
    }
}
