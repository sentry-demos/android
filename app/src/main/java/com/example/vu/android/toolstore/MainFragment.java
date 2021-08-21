package com.example.vu.android.toolstore;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.ProgressDialog;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.sentry.Attachment;
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

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.vu.android.R;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment implements StoreItemAdapter.ItemClickListener {
    protected List<StoreItem> toolStoreItems = new ArrayList<StoreItem>();
    private List<StoreItem> list;
    private RecyclerView mList;
    private DividerItemDecoration dividerItemDecoration;
    private List<StoreItem> selectedStoreItems;
    protected StoreItemAdapter adapter;
    ProgressDialog progressDialog = null;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    //private ArrayList<DataModel> list = new ArrayList<>();
    public String END_POINT_TOOLS = "/tools";
    public String END_POINT_CHECKOUT = "/checkout";

    TextView textCartItemCount;
    int mCartItemCount = -1;
    public MainFragment() {
        // Required empty public constructor
        this.fetchToolsFromServer();




    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }



    public void setBadgeNumber(){
        textCartItemCount.setText(String.valueOf(++mCartItemCount));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=  inflater.inflate(R.layout.fragment_main, container, false);
        initRecyclerView(view);
        fetchToolsFromServer();
        return view;
    }

    private void initRecyclerView(View view) {
        mList = view.findViewById(R.id.main_list);
        StoreItemAdapter adapter = new StoreItemAdapter(list, this);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);


        dividerItemDecoration = new DividerItemDecoration(mList.getContext(), layoutManager.getOrientation());

        mList.setHasFixedSize(true);
        mList.setLayoutManager(layoutManager);
        mList.addItemDecoration(dividerItemDecoration);
        mList.setAdapter(adapter);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    //Might have to add this eventually
//    private void loadListLayout(){
//        mList = findViewById(R.id.main_list);
//        adapter = new StoreItemAdapter(this);
//
//        linearLayoutManager = new LinearLayoutManager(this);
//        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        dividerItemDecoration = new DividerItemDecoration(mList.getContext(), linearLayoutManager.getOrientation());
//
//        mList.setHasFixedSize(true);
//        mList.setLayoutManager(linearLayoutManager);
//        mList.addItemDecoration(dividerItemDecoration);
//        mList.setAdapter(adapter);
//    }


    public void fetchToolsFromServer() {
        //I think this will work?
        progressDialog = new ProgressDialog(getActivity().getApplicationContext());
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        ISpan transaction = Sentry.getSpan();
        ISpan httpSpan = transaction.startChild("http.client", "fetch tools from server");

        String domain = this.getToolStoreDomain();
        String getToolsURL = domain + this.END_POINT_TOOLS;

        SentryTraceHeader sentryTraceHeader = httpSpan.toSentryTrace();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(getToolsURL)
                .header(sentryTraceHeader.getName(), sentryTraceHeader.getValue())
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                progressDialog.dismiss();
                if(response.isSuccessful()){
                    String responseStr = response.body().string();
                    //I think this will work, getActivity( = ToolStoreActivity.this
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            httpSpan.finish(SpanStatus.OK);

                            if (responseStr != null && !responseStr.equals("")) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ISpan taskSpan = transaction.startChild("task", "Process server response.");

                                        processGetToolsResponse(responseStr);

                                        taskSpan.finish(SpanStatus.OK);
                                    }
                                });
                            }
                            transaction.finish(SpanStatus.OK);
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                progressDialog.dismiss();
                httpSpan.setThrowable(e);
                httpSpan.finish(SpanStatus.INTERNAL_ERROR);
                transaction.finish(SpanStatus.INTERNAL_ERROR);
            }
        });
    }

    @Override
    public void onItemClick(StoreItem storeItem) {
        //StoreItem selectedItem = list.get(holder.getAdapterPosition());
        //selectedStoreItems.add(selectedItem);
        setBadgeNumber();
        //TODO: might not need this
        adapter.notifyDataSetChanged();
    }

    private void processGetToolsResponse(String body) {

        JSONObject jsonObject = null;
        try {
            JSONArray jsonArray = new JSONArray(body);

            for(int i = 0; i < jsonArray.length(); i++){
                jsonObject = jsonArray.getJSONObject(i);
                StoreItem storeitem = new StoreItem();
                storeitem.setName(jsonObject.getString("name"));
                storeitem.setSku(jsonObject.getString("sku"));
                storeitem.setPrice(jsonObject.getInt("price"));
                storeitem.setImage(jsonObject.getString("image"));
                storeitem.setType(jsonObject.getString("type"));
                storeitem.setItemId(jsonObject.getInt("id"));

                toolStoreItems.add(storeitem);
            }
        } catch (JSONException e) {
            ISpan span = Sentry.getSpan();
            if (span != null) {
                span.setThrowable(e);
                span.setStatus(SpanStatus.INTERNAL_ERROR);
                span.finish();
                Sentry.captureException(e);
            }
        }
        finally {
            adapter.notifyDataSetChanged();
        }
    }

    private String getToolStoreDomain() {
        String domain = null;
        try {
            final ApplicationInfo appInfo = getActivity().getApplicationContext().getPackageManager().getApplicationInfo(getActivity().getApplicationContext().getPackageName(),
                    PackageManager.GET_META_DATA);

            if (appInfo.metaData != null) {
                domain = (String) appInfo.metaData.get("toolstore.domain");
            }
        } catch (Exception e) {
            Sentry.captureException(e);
        }
        return domain;
    }

    public void checkout(List<StoreItem> selectedStoreItems){
        ITransaction checkoutTransaction = Sentry.startTransaction("checkout [android]", "http.client");
        checkoutTransaction.setOperation("http");
        Sentry.configureScope(scope -> scope.setTransaction(checkoutTransaction));

        final ProgressDialog progressDialog = new ProgressDialog(getActivity().getApplicationContext());
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
                    //I think this will work?
                    getActivity().runOnUiThread(new Runnable() {
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

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_cart:
                this.checkout(this.adapter.getSelectedStoreItems());
                return(true);

        }
        return(super.onOptionsItemSelected(item));
    }

    private void processDeliveryItem(ITransaction checkoutTransaction){
        ISpan processDeliverySpan = checkoutTransaction.startChild("task", "process delivery");

        try{
            throw new MainFragment.ItemDeliveryProcessException("Failed to init delivery workflow");
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

    class ItemDeliveryProcessException extends RuntimeException{

        public ItemDeliveryProcessException(String message){
            super(message);
        }
    }

    protected Boolean addAttachment() {
        File f = null;
        try {
            Context c = getActivity().getApplicationContext();
            File cacheDirectory = c.getCacheDir();
            f = File.createTempFile("tmp", ".txt", cacheDirectory);
            System.out.println("File path: "+f.getAbsolutePath());
            f.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write("test".getBytes(UTF_8));
            }
            String dateStr = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());

            Attachment attachment1 = new Attachment(f.getAbsolutePath(), "tmp_"+dateStr+".txt", "text/plain");

            Sentry.configureScope(
                    scope -> {
                        String json = "{ \"number\": 10 }";
                        Attachment attachment2 = new Attachment(json.getBytes(), "log_"+dateStr+".json", "text/plain");
                        scope.addAttachment(attachment1);
                        scope.addAttachment(attachment2);
                    });
        } catch(Exception e) {
            Sentry.captureException(e);
            e.printStackTrace();
        }
        return true;
    }



}