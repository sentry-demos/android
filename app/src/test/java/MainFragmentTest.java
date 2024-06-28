package com.example.vu.android.empowerplant;

import android.content.Context;
import android.os.Bundle;
import androidx.lifecycle.Lifecycle;
import io.sentry.ITransaction;
import io.sentry.Sentry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;


import java.util.HashMap;

public class MainFragmentTest {

    private MainFragment mainFragment;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mainFragment = spy(MainFragment.newInstance());
    }

    @Test
    public void testMainFragmentNotNull() {
        assertNotNull(mainFragment);
    }

    @Test
    public void testGetNumber() {
        // Given
        doReturn(2).when(mainFragment).getAnotherNumber();

        // When
        int result = mainFragment.getNumber();

        // Then
        assertEquals(2, result);
        verify(mainFragment, times(1)).getAnotherNumber();
    }

    @Test
    public void testGetAnotherNumber() {
        // When
        int result = mainFragment.getAnotherNumber();

        // Then
        assertEquals(2, result);
    }
}
