package com.example.vu.android.toolstore;

import android.app.ProgressDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

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
import com.example.vu.android.MyBaseActivity;
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


public class ToolStoreActivity extends MyBaseActivity {

    public String END_POINT_TOOLS = "/tools";
    public String END_POINT_CHECKOUT = "/checkout";

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

        setContentView(R.layout.activity_toolstore);
        this.loadListLayout();
        this.fetchToolsFromServer();
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

    private String getToolStoreDomain(){
        String domain = null;
        try {
            final ApplicationInfo appInfo = getApplicationContext().getPackageManager().getApplicationInfo(getApplicationContext().getPackageName(),
                    PackageManager.GET_META_DATA);

            if (appInfo.metaData != null) {
                domain = (String) appInfo.metaData.get("toolstore.domain");
            }
        }
        catch (Exception e) {
            //Log.w("Couldn't find meta-data: " + name);
            Sentry.captureException(e);
        }
        return domain;
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


    private void loadListLayout(){
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

    public void fetchToolsFromServer(){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        ISpan span = Sentry.getSpan();
        ISpan httpSpan = span.startChild("http.client", "fetch tools from server");

        SentryTraceHeader httpSpanHeader= httpSpan.toSentryTrace();

        String domain = this.getToolStoreDomain();
        String getToolsURL = domain + this.END_POINT_TOOLS;

        // Get a RequestQueue
        RequestQueue queue = RequestQueueSingleton.getInstance(this.getApplicationContext()).
                getRequestQueue();

        JsonArrayRequest jsonArrayRequest =
            new JsonArrayRequest(Request.Method.GET, getToolsURL, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray jsonArray) {
                            httpSpan.finish(SpanStatus.OK);
                            ISpan processResponseSpan = span.startChild("task", "process tools from server");

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
                                processResponseSpan.setThrowable(e);
                                processResponseSpan.setStatus(SpanStatus.INTERNAL_ERROR);
                            }
                            finally {
                                adapter.notifyDataSetChanged();
                                if(processResponseSpan.getStatus() !=  SpanStatus.INTERNAL_ERROR){
                                    processResponseSpan.finish(SpanStatus.OK);
                                }
                                span.finish(SpanStatus.OK);
                                progressDialog.dismiss();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    httpSpan.setThrowable(error);
                    httpSpan.finish(SpanStatus.INTERNAL_ERROR);
                    span.finish(SpanStatus.INTERNAL_ERROR);
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String>  headers = new HashMap<>();
                    headers.put(httpSpanHeader.getName(),httpSpanHeader.getValue());
                    return headers;
                }
            };

            jsonArrayRequest.setRetryPolicy(new ToolstoreRetryPolicy());
            RequestQueueSingleton.getInstance(this.getApplicationContext()).addToRequestQueue(jsonArrayRequest);
    }


    public void checkout(List<StoreItem> selectedStoreItems){
        ITransaction checkoutTransaction = Sentry.startTransaction("checkout [android]", "http.client");
        checkoutTransaction.setOperation("http");
        Sentry.configureScope(scope -> scope.setTransaction(checkoutTransaction));


        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Checking Out...");
        progressDialog.show();

        ISpan processDataSpan = checkoutTransaction.startChild("task", "process_cart_data");
        JSONObject object = this.buildJSONPostData(selectedStoreItems);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        processDataSpan.finish();

        ISpan httpSpan = checkoutTransaction.startChild("http.client", "call checkout");
        SentryTraceHeader httpSpanHeaders = httpSpan.toSentryTrace();
        RequestQueue queue = RequestQueueSingleton.getInstance(this.getApplicationContext()).
                getRequestQueue();

        String domain = this.getToolStoreDomain();
        String checkoutURL = domain + this.END_POINT_CHECKOUT;

        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.POST, checkoutURL, object,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                progressDialog.dismiss();
                                httpSpan.finish(SpanStatus.OK);

                                ISpan processResponseSpan = checkoutTransaction.startChild("task", "process response");
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                                processResponseSpan.finish(SpanStatus.OK);
                                checkoutTransaction.finish(SpanStatus.OK);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        httpSpan.setThrowable(error);
                        httpSpan.finish(SpanStatus.INTERNAL_ERROR);

                        processDeliveryItem(checkoutTransaction);

                        checkoutTransaction.finish(SpanStatus.INTERNAL_ERROR);
                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String>  headers = new HashMap<>();
                        headers.put(httpSpanHeaders.getName(),httpSpanHeaders.getValue());
                        headers.put("email", "someone@gmail.com");
                        return headers;
                    }
                };

        jsonObjectRequest.setRetryPolicy(new ToolstoreRetryPolicy());

        RequestQueueSingleton.getInstance(this.getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    private void processDeliveryItem(ITransaction checkoutTransaction){
        ISpan processDeliverySpan = checkoutTransaction.startChild("task", "process delivery");

        try{
            throw new ItemDeliveryProcessException("Failed to init delivery workflow");
        }catch(Exception e){
            addAttachment();
            processDeliverySpan.setThrowable(e);
            processDeliverySpan.setStatus(SpanStatus.INTERNAL_ERROR);
            Sentry.captureException(e);
        }

        if(processDeliverySpan.getStatus() !=  SpanStatus.INTERNAL_ERROR){
            processDeliverySpan.setStatus(SpanStatus.OK);
        }
        processDeliverySpan.finish();
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

    class ItemDeliveryProcessException extends RuntimeException{

        public ItemDeliveryProcessException(String message){
            super(message);
        }
    }
}