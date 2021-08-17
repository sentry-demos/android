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

    private RecyclerView mList;
    private LinearLayoutManager linearLayoutManager;
    private DividerItemDecoration dividerItemDecoration;
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

    private String getToolStoreDomain() {
        String domain = null;
        try {
            final ApplicationInfo appInfo = getApplicationContext().getPackageManager().getApplicationInfo(getApplicationContext().getPackageName(),
                    PackageManager.GET_META_DATA);

            if (appInfo.metaData != null) {
                domain = (String) appInfo.metaData.get("toolstore.domain");
            }
        } catch (Exception e) {
            Sentry.captureException(e);
        }
        return domain;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_cart:
                this.checkout(this.adapter.getSelectedStoreItems());
                return(true);

        }
        return(super.onOptionsItemSelected(item));
    }

    //add fragment here
    private void loadFragmentList(){
        //mList references RecyclerView from activity_toolstore.xml
        //mList = findViewById(R.id.main_list);
        fragment = MainFragment.newInstance();





        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        dividerItemDecoration = new DividerItemDecoration(mList.getContext(), linearLayoutManager.getOrientation());

        mList.setHasFixedSize(true);
        mList.setLayoutManager(linearLayoutManager);
        mList.addItemDecoration(dividerItemDecoration);
//        mList.setAdapter(adapter);
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

        String domain = this.getToolStoreDomain();
        String checkoutURL = domain + this.END_POINT_CHECKOUT;

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        RequestBody body = RequestBody.create(object.toString(), JSON);

        Request request = new Request.Builder()
                .url(checkoutURL)
                .header(httpSpanHeaders.getName(), httpSpanHeaders.getValue())
                .header("email", "someone@gmail.com")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                progressDialog.dismiss();
                if(!response.isSuccessful()){
                    ToolStoreActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            httpSpan.finish(SpanStatus.INTERNAL_ERROR);

                            processDeliveryItem(checkoutTransaction);

                            checkoutTransaction.finish(SpanStatus.INTERNAL_ERROR);
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                progressDialog.dismiss();
                httpSpan.setThrowable(e);
                httpSpan.finish(SpanStatus.INTERNAL_ERROR);
                httpSpan.finish();
                Sentry.captureException(e);

                processDeliveryItem(checkoutTransaction);
                checkoutTransaction.finish(SpanStatus.INTERNAL_ERROR);
            }
        });
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
                jsonObject.put("id", s.getItemId());

                jsonArray.put(jsonObject);
            }
            response.put("cart",jsonArray);
        } catch (JSONException e) {
            ISpan span = Sentry.getSpan();
            if(span != null){
                span.setThrowable(e);
                span.finish(SpanStatus.INTERNAL_ERROR);
                span.finish();
            }
            Sentry.captureException(e);
        }
        return response;
    }



    class ItemDeliveryProcessException extends RuntimeException{

        public ItemDeliveryProcessException(String message){
            super(message);
        }
    }


}