package com.example.vu.android.empowerplant;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;

import com.example.vu.android.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class MainFragmentTest {

    @Rule
    public FragmentScenario<MainFragment> scenario = FragmentScenario.launchInContainer(MainFragment.class);

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @Test
    public void testRecyclerViewIsDisplayed() {
        Espresso.onView(withId(R.id.recyclerView))
                .check(ViewAssertions.matches(isDisplayed()));
    }

    @Test
    public void testFetchToolsFromServer() throws Exception {
        MockResponse response = new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"id\":1,\"title\":\"Item 1\",\"price\":100,\"imgcropped\":\"url1\"}]");
        mockWebServer.enqueue(response);

        scenario.onFragment(fragment -> {
            fragment.END_POINT_PRODUCTS = mockWebServer.url("/products").toString();
            fragment.fetchToolsFromServer();
        });

        Espresso.onView(withText("Item 1"))
                .check(ViewAssertions.matches(isDisplayed()));
    }

    @Test
    public void testCheckoutButton() {
        // Assuming there is a checkout button in the fragment's layout
        Espresso.onView(withId(R.id.checkout_button))
                .perform(ViewActions.click());

        // Verify the progress dialog is displayed
        Espresso.onView(withText("Checking Out..."))
                .check(ViewAssertions.matches(isDisplayed()));
    }
}
