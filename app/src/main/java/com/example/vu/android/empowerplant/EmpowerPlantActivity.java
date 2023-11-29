package com.example.vu.android.empowerplant;


import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentTransaction;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.example.vu.android.MainActivity;
import com.example.vu.android.MyBaseActivity;
import com.example.vu.android.R;

import io.sentry.ISpan;
import io.sentry.Sentry;

public class EmpowerPlantActivity extends MyBaseActivity {

    static boolean active = false;
    MainFragment fragment = null;
    TextView textCartItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_empowerplant);
        dbQuery();
        addAttachment(true);
        this.loadFragmentList();
    }

    public void dbQuery() {
        List<StoreItem> tmpStoreItems = new ArrayList<StoreItem>();
        for (int i = 0; i < 60; i++) {
                StoreItem storeitem = new StoreItem();
                storeitem.setName(genRandomString());
                storeitem.setSku(genRandomString());
                storeitem.setPrice(i);
                storeitem.setImage(genRandomString());
                storeitem.setItemId(i);
                storeitem.setQuantity(1);
                tmpStoreItems.add(storeitem);
            }
        
        AppDatabase.getInstance(getApplicationContext())
                .StoreItemDAO().insertAll(tmpStoreItems);
                
        AppDatabase.getInstance(getApplicationContext())
                .StoreItemDAO().deleteAll();
        
    }

    // Generates a randome string of characters from a to z
    private String genRandomString() {
        byte[] array = new byte[200];
        Random r = new Random();
        for (int index = 0; index < array.length; index++) {
            int b = r.nextInt(26);
            b += 97;
            array[index] = ((byte) b);
        }
        return new String(array);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.empowerplant_toplevel, menu);

        final MenuItem menuItem = menu.findItem(R.id.action_cart);
        View actionView = menuItem.getActionView();
        textCartItemCount = (TextView) actionView.findViewById(R.id.cart_badge);

        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(menuItem);
            }
        });

        return true;
    }

    private void loadFragmentList() {
        fragment = MainFragment.newInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_list, fragment, "main_fragment");
        transaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_cart:
                fragment.checkout();
                return(true);
            case R.id.action_open_listapp:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
        }
        return(super.onOptionsItemSelected(item));
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }
}