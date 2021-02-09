package com.example.vu.android.toolstore;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
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


public class ToolStoreActivity extends AppCompatActivity {

    public static String END_POINT_TOOLS = "https://neilmanvar-flask-m3uuizd7iq-uc.a.run.app/tools";
    public static String END_POINT_CHECKOUT = "https://neilmanvar-flask-m3uuizd7iq-uc.a.run.app/checkout";

    private RecyclerView mList;
    private LinearLayoutManager linearLayoutManager;
    private DividerItemDecoration dividerItemDecoration;
    protected StoreItemAdapter adapter;
    private Menu menu;
    protected List<StoreItem> toolStoreItems = new ArrayList<StoreItem>();

    TextView textCartItemCount;
    int mCartItemCount = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ITransaction transaction = Sentry.startTransaction("toolstore [android]");
        transaction.setOperation("activity_load");

        ISpan span = transaction.startChild("task", "Load layout elements");


        setContentView(R.layout.activity_toolstore);
        this.loadListLayout(transaction);
        this.fetchToolsFromServer(transaction);
        span.finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolstore_toplevel, menu);

        final MenuItem menuItem = menu.findItem(R.id.action_cart);
        View actionView = menuItem.getActionView();
        textCartItemCount = (TextView) actionView.findViewById(R.id.cart_badge);
        setBadgeNumber();

        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(menuItem);
            }
        });

        return true;
    }

    protected void setBadgeNumber(){
        textCartItemCount.setText(String.valueOf(++mCartItemCount));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_cart:
                this.OnCheckoutClick();
                return(true);

        }
        return(super.onOptionsItemSelected(item));
    }

    private void OnCheckoutClick(){
        this.checkout(this.adapter.getSelectedStoreItems());
    }


    private void loadListLayout(ITransaction transaction){
        mList = findViewById(R.id.main_list);
        adapter = new StoreItemAdapter(this);

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        dividerItemDecoration = new DividerItemDecoration(mList.getContext(), linearLayoutManager.getOrientation());

        mList.setHasFixedSize(true);
        mList.setLayoutManager(linearLayoutManager);
        mList.addItemDecoration(dividerItemDecoration);
        mList.setAdapter(adapter);
    }

    public void fetchToolsFromServer(ITransaction transaction){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        ISpan span = transaction.startChild("http", "fetch tools from server");
        SentryTraceHeader spanHeader= span.toSentryTrace();

        // Get a RequestQueue
        RequestQueue queue = RequestQueueSigleton.getInstance(this.getApplicationContext()).
                getRequestQueue();

        JsonArrayRequest jsonArrayRequest =
                new JsonArrayRequest(Request.Method.GET, this.END_POINT_TOOLS, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray jsonArray) {
                                span.finish(SpanStatus.OK);

                                ISpan span2 = transaction.startChild("task", "process tools from server");
                                JSONObject jsonObject = null;
                                try {
                                    for(int i = 0; i < jsonArray.length(); i++){
                                        jsonObject = jsonArray.getJSONObject(i);
                                        StoreItem storeitem = new StoreItem();
                                        storeitem.setName(jsonObject.getString("name"));
                                        storeitem.setSku(jsonObject.getString("sku"));
                                        storeitem.setPrice(jsonObject.getInt("price"));
                                        storeitem.setImage(jsonObject.getString("image"));
                                        storeitem.setType(jsonObject.getString("type"));
                                        storeitem.setId(jsonObject.getInt("id"));

                                        toolStoreItems.add(storeitem);
                                    }
                                } catch (JSONException e) {
                                    span2.setThrowable(e);
                                    span2.setStatus(SpanStatus.INTERNAL_ERROR);
                                }
                                finally {
                                    adapter.notifyDataSetChanged();
                                    if(span2.getStatus() !=  SpanStatus.INTERNAL_ERROR){
                                        span2.finish(SpanStatus.OK);
                                    }
                                    transaction.finish(SpanStatus.OK);
                                    progressDialog.dismiss();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        span.setThrowable(error);
                        span.finish(SpanStatus.INTERNAL_ERROR);
                        transaction.finish(SpanStatus.INTERNAL_ERROR);
                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String>  headers = new HashMap<>();
                        headers.put(spanHeader.getName(),spanHeader.getValue());
                        return headers;
                    }
                };

        jsonArrayRequest.setRetryPolicy(new ToolstoreRetryPolicy());

        RequestQueueSigleton.getInstance(this.getApplicationContext()).addToRequestQueue(jsonArrayRequest);
    }


    public void checkout(List<StoreItem> selectedStoreItems){
        ITransaction transaction = Sentry.startTransaction("checkout [android]");
        transaction.setOperation("http");

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Checking Out...");
        progressDialog.show();

        ISpan span = transaction.startChild("task", "process_cart_data");
        JSONObject object = this.buildJSONPostData(selectedStoreItems);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        span.finish();

        ISpan span2 = transaction.startChild("http", "call checkout");
        SentryTraceHeader span2Headers = span2.toSentryTrace();
        RequestQueue queue = RequestQueueSigleton.getInstance(this.getApplicationContext()).
                getRequestQueue();

        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.POST, this.END_POINT_CHECKOUT, object,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                progressDialog.dismiss();
                                span2.finish(SpanStatus.OK);

                                ISpan span3 = transaction.startChild("task", "process response");
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                                span3.finish(SpanStatus.OK);
                                transaction.finish(SpanStatus.OK);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ToolStoreActivity.this);
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.setCancelable(true);
                        alertDialog.setTitle("Checkout Failed!");
                        alertDialog.setMessage("Server Error");
                        alertDialog.show();

                        span2.setThrowable(error);
                        span2.finish(SpanStatus.INTERNAL_ERROR);
                        transaction.finish(SpanStatus.INTERNAL_ERROR);
                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String>  headers = new HashMap<>();
                        headers.put(span2Headers.getName(),span2Headers.getValue());
                        headers.put("email", "someone@gmail.com");
                        return headers;
                    }
                };

        jsonObjectRequest.setRetryPolicy(new ToolstoreRetryPolicy());

        RequestQueueSigleton.getInstance(this.getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    private JSONObject buildJSONPostData(List<StoreItem> selectedStoreItems){
        JSONObject jsonObject, response = new JSONObject();
        JSONArray jsonArray  = new JSONArray();
        try {
            for(StoreItem s:selectedStoreItems){
                jsonObject = new JSONObject();
                jsonObject.put("name", s.getName());
                jsonObject.put("sku", s.getSku());
                jsonObject.put("price", s.getPrice());
                jsonObject.put("image", s.getImage());
                jsonObject.put("type", s.getType());
                jsonObject.put("id", s.getId());

                jsonArray.put(jsonObject);
            }
            response.put("cart",jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return response;
    }


    private class ToolstoreRetryPolicy implements RetryPolicy {
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
    }
}