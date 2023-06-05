package com.example.vu.android;

import static org.junit.Assert.*;

import org.junit.Test;

public class MyApplicationTest {

    @org.junit.Test
    public void getcurrentactivity() {
        MyApplication app = new MyApplication();
        assertEquals(app.getCurrentActivity(), null);
    }
}