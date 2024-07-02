package com.example.vu.android.empowerplant;

import android.content.Context;
import android.content.ContextWrapper;

public class MockContext extends ContextWrapper {

    public MockContext() {
        super(null);
    }

    @Override
    public Context getApplicationContext() {
        return this;
    }

    // Add other methods if needed for your specific tests
}
