package com.example.vu.android.empowerplant;

import android.content.Context;
import android.os.Bundle;
import androidx.lifecycle.Lifecycle;
import io.sentry.ITransaction;
import io.sentry.Sentry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

public class MainFragmentTest {

    private MainFragment mainFragment;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mainFragment = MainFragment.newInstance();
    }

    @Test
    public void testMainFragmentNotNull() {
        assertNotNull(mainFragment);
    }
}
