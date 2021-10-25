package com.example.vu.android.toolstore;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
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
import io.sentry.android.okhttp.SentryOkHttpInterceptor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.example.vu.android.R;



/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment implements StoreItemAdapter.ItemClickListener {
    protected List<StoreItem> toolStoreItems = new ArrayList<StoreItem>();
    private DividerItemDecoration dividerItemDecoration;
    private List<StoreItem> selectedStoreItems;
    protected StoreItemAdapter adapter;
    ProgressDialog progressDialog = null;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public String END_POINT_TOOLS = "/tools";
    public String END_POINT_CHECKOUT = "/checkout";
    int mCartItemCount = 0;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity a;
        if (context instanceof Activity){
            a=(Activity) context;
        }
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=  inflater.inflate(R.layout.fragment_main, container, false);
        initRecyclerView(view);
        return view;
    }

    private void initRecyclerView(View view) {
        this.fetchToolsFromServer();
        adapter = new StoreItemAdapter(toolStoreItems, this);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    public void setBadgeNumber(){
        ((ToolStoreActivity) getActivity()).textCartItemCount.setText(String.valueOf(++mCartItemCount));
    }

    public void fetchToolsFromServer() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        ISpan transaction = Sentry.getSpan();
        ISpan httpSpan = transaction.startChild("http.client", "fetch tools from server");

        String domain = this.getToolStoreDomain();
        String getToolsURL = domain + this.END_POINT_TOOLS;

        SentryTraceHeader sentryTraceHeader = httpSpan.toSentryTrace();

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(SentryOkHttpInterceptor())
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

    public void checkout(){
        selectedStoreItems = this.adapter.getSelectedStoreItems();
        ITransaction checkoutTransaction = Sentry.startTransaction("checkout [android]", "http.client");
        checkoutTransaction.setOperation("http");
        Sentry.configureScope(scope -> scope.setTransaction(checkoutTransaction));

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
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
                .addInterceptor(SentryOkHttpInterceptor())
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