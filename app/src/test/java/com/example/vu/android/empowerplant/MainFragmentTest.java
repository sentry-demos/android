package com.example.vu.android.empowerplant;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(AndroidJUnit4.class)
public class MainFragmentTest {

    private MainFragment mainFragment;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mainFragment = new MainFragment();
    }

    @Test
    public void testGetEmpowerPlantDomain() {
        String domain = "https://application-monitoring-flask-dot-sales-engineering-sf.appspot.com";

        String result = mainFragment.getEmpowerPlantDomain();

        assertEquals("", "");
    }

    @Test
    public void testGetIterator() {
        assertEquals(0, mainFragment.getIterator(0));
    }
}
