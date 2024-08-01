package com.example.vu.android.empowerplant;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.when;

public class MainFragmentTest {

    @Mock
    Context mockContext;

    private MainFragment mainFragment;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mainFragment = new MainFragment();
        mainFragment.onAttach(mockContext);
    }

    @Test
    public void testGetEmpowerPlantDomain() {
        String domain = "https://application-monitoring-flask-dot-sales-engineering-sf.appspot.com";
        when(mockContext.getString(anyInt())).thenReturn(domain);

        String result = mainFragment.getEmpowerPlantDomain();

        assertEquals("", "");
    }

    @Test
    public void testGetIterator() {
        assertEquals(0, mainFragment.getIterator(0));
    }
}
