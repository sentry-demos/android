package com.example.vu.android.toolstore;

import android.app.ProgressDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.vu.android.MyBaseActivity;
import com.example.vu.android.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.sentry.ISpan;
import io.sentry.ITransaction;
import io.sentry.Sentry;
import io.sentry.SentryTraceHeader;
import io.sentry.SpanStatus;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ToolStoreActivity extends MyBaseActivity {

    public String END_POINT_TOOLS = "/tools";
    public String END_POINT_CHECKOUT = "/checkout";

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

//    private RecyclerView mList;
//    private LinearLayoutManager linearLayoutManager;
//    private DividerItemDecoration dividerItemDecoration;
    protected StoreItemAdapter adapter;
    private Menu menu;
    protected List<StoreItem> toolStoreItems = new ArrayList<StoreItem>();
    protected MainFragment fragment;
    ProgressDialog progressDialog = null;

    TextView textCartItemCount;
    int mCartItemCount = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_toolstore);
        //this.loadListLayout();
        this.loadFragmentList();
        //fetching tools from Fragment
        //this.fetchToolsFromServer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolstore_toplevel, menu);

        final MenuItem menuItem = menu.findItem(R.id.action_cart);
        View actionView = menuItem.getActionView();
        textCartItemCount = (TextView) actionView.findViewById(R.id.cart_badge);
        fragment.setBadgeNumber();

        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(menuItem);
            }
        });

        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch(item.getItemId()) {
//            case R.id.action_cart:
//                this.checkout(this.adapter.getSelectedStoreItems());
//                return(true);
//
//        }
//        return(super.onOptionsItemSelected(item));
//    }

    //add fragment here
    private void loadFragmentList(){
        //mList references RecyclerView from activity_toolstore.xml
        //mList = findViewById(R.id.main_list);
        fragment = MainFragment.newInstance();





//        linearLayoutManager = new LinearLayoutManager(this);
//        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        dividerItemDecoration = new DividerItemDecoration(mList.getContext(), linearLayoutManager.getOrientation());
//
//        mList.setHasFixedSize(true);
//        mList.setLayoutManager(linearLayoutManager);
//        mList.addItemDecoration(dividerItemDecoration);
//        mList.setAdapter(adapter);
    }













}