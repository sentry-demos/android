package com.example.vu.android.toolstore;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.vu.android.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.sentry.ISpan;
import io.sentry.ITransaction;
import io.sentry.Sentry;
import io.sentry.SentryTraceHeader;
import io.sentry.SpanStatus;

public class ToolstoreActivity extends AppCompatActivity {

    public static String END_POINT_TOOLS = "https://neilmanvar-flask-m3uuizd7iq-uc.a.run.app/tools";
    private RecyclerView mList;

    private LinearLayoutManager linearLayoutManager;
    private DividerItemDecoration dividerItemDecoration;
    private List<StoreItem> storeItemList;
    private RecyclerView.Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toolstore);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Get the Intent that started this activity and extract the string
        //Intent intent = getIntent();

        mList = findViewById(R.id.main_list);

        storeItemList = new ArrayList<>();
        adapter = new StoreItemAdapter(getApplicationContext(),storeItemList);

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        dividerItemDecoration = new DividerItemDecoration(mList.getContext(), linearLayoutManager.getOrientation());

        mList.setHasFixedSize(true);
        mList.setLayoutManager(linearLayoutManager);
        mList.addItemDecoration(dividerItemDecoration);
        mList.setAdapter(adapter);

        this.fetchTools();
    }

    private void fetchTools(){
        ITransaction transaction = Sentry.startTransaction("toolstore_load");
        transaction.setOperation("activity_load");

        ISpan innerSpan = transaction.startChild("task", "show progress bar");
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        innerSpan.finish();

        ISpan innerSpan2 = transaction.startChild("task", "fetch tools from server");
        SentryTraceHeader innerSpan2Header= innerSpan2.toSentryTrace();

        // Get a RequestQueue
        RequestQueue queue = RequestQueueSigleton.getInstance(this.getApplicationContext()).
                getRequestQueue();

        JsonArrayRequest jsonArrayRequest =
                new JsonArrayRequest(Request.Method.GET, this.END_POINT_TOOLS, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray jsonArray) {
                                innerSpan2.finish(SpanStatus.OK);
                                ISpan innerSpan3 = transaction.startChild("task", "process tools from server");

                                try {
                                    for(int i = 0; i < jsonArray.length(); i++){
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                                        StoreItem storeitem = new StoreItem();
                                        storeitem.setName(jsonObject.getString("name"));
                                        storeitem.setSku(jsonObject.getString("sku"));
                                        storeitem.setPrice(jsonObject.getInt("price"));
                                        storeitem.setImage(jsonObject.getString("image"));

                                        storeItemList.add(storeitem);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    progressDialog.dismiss();
                                    innerSpan3.setThrowable(e);
                                    innerSpan3.setStatus(SpanStatus.INTERNAL_ERROR);
                                }finally {
                                    adapter.notifyDataSetChanged();
                                    progressDialog.dismiss();
                                    if(innerSpan3.getStatus() !=  SpanStatus.INTERNAL_ERROR){
                                        innerSpan3.finish(SpanStatus.OK);
                                    }
                                    transaction.finish(SpanStatus.OK);
                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley", error.toString());
                        progressDialog.dismiss();
                        error.printStackTrace();
                        innerSpan2.setThrowable(error);
                        innerSpan2.finish(SpanStatus.INTERNAL_ERROR);
                        transaction.finish(SpanStatus.INTERNAL_ERROR);
                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String>  headers = new HashMap<>();
                        headers.put(innerSpan2Header.getName(),innerSpan2Header.getValue());
                        return headers;
                    }
                };

        jsonArrayRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 15000;
            }

            @Override
            public int getCurrentRetryCount() {
                return DefaultRetryPolicy.DEFAULT_MAX_RETRIES;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError { }
        });


        RequestQueueSigleton.getInstance(this).addToRequestQueue(jsonArrayRequest);

    }

}