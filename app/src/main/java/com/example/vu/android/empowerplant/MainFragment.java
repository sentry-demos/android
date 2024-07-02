package com.example.vu.android.empowerplant;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.sentry.Attachment;
import io.sentry.ISpan;
import io.sentry.ITransaction;
import io.sentry.Sentry;
import io.sentry.SpanStatus;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
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
    protected List<StoreItem> empowerStoreItems = new ArrayList<StoreItem>();
    private DividerItemDecoration dividerItemDecoration;
    private HashMap<String, StoreItem> selectedStoreItems;
    protected StoreItemAdapter adapter;
    ProgressDialog progressDialog = null;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public String END_POINT_PRODUCTS = "/products";
    public String END_POINT_PRODUCT_INFO = "/product/0/info";
    public String END_POINT_CHECKOUT = "/checkout";
    int mCartItemCount = 0;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity a;
        if (context instanceof Activity) {
            a = (Activity) context;
        }
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
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
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        initRecyclerView(view);
        return view;
    }

    private void initRecyclerView(View view) {
        this.fetchToolsFromServer();
        adapter = new StoreItemAdapter(empowerStoreItems, this);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    public void setBadgeNumber() {
        ((EmpowerPlantActivity) getActivity()).textCartItemCount.setText(String.valueOf(++mCartItemCount));
    }

    public void fetchToolsFromServer() {
        progressDialog = new ProgressDialog(getContext());//ProgressDialog has been deprecated in API 26 https://developer.android.com/reference/android/app/ProgressDialog
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        ISpan transaction = Sentry.getSpan();
        ISpan productRetrieveSpan = transaction.startChild("product_retrieval", "Product Retrieval");

        String domain = this.getEmpowerPlantDomain();
        String getToolsURL = domain + this.END_POINT_PRODUCTS;

        Request request = new Request.Builder()
                .url(getToolsURL)
                .build();

        OkHttpClient client = new RequestClient().getClient();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    String responseStr = response.body().string();

                    progressDialog.dismiss();//why called a second time

                    if (responseStr != null && !responseStr.equals("")) {

                        ISpan taskSpan = productRetrieveSpan.startChild("task", "Process server response.");

                        processGetToolsResponse(responseStr);

                        taskSpan.finish(SpanStatus.OK);

                        ISpan dbSpan = productRetrieveSpan.startChild("db.insertAll", "Persist Store Items");
                        insertMultipleStoreItems();
                        dbSpan.finish();
                    }


                    ISpan processProductsSpan = Sentry.getSpan().startChild("product_processing", "Product Processing");
                    getActivity().runOnUiThread(() -> {
                        processProducts();
                        processProductsInfo();
                        Sentry.reportFullyDisplayed();
                        processProductsSpan.finish();
                        productRetrieveSpan.finish();
                        Sentry.getCurrentHub().getSpan().finish();//finish Empower txn manually
                    });

                } else {
                    productRetrieveSpan.finish();
                    Sentry.reportFullyDisplayed();
                }


            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                progressDialog.dismiss();
                Sentry.reportFullyDisplayed();
                productRetrieveSpan.finish();
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

            for (int i = 0; i < jsonArray.length(); i++) {

                jsonObject = jsonArray.getJSONObject(i);
                StoreItem storeitem = new StoreItem();
                storeitem.setName(jsonObject.getString("title"));
                storeitem.setSku(jsonObject.getString("id"));
                storeitem.setPrice(jsonObject.getInt("price"));
                storeitem.setImage(jsonObject.getString("imgcropped"));
                storeitem.setItemId(jsonObject.getInt("id"));
                storeitem.setQuantity(1);

                empowerStoreItems.add(storeitem);
            }
        } catch (JSONException e) {
            ISpan span = Sentry.getSpan();
            if (span != null) {
                span.setThrowable(e);
                span.setStatus(SpanStatus.INTERNAL_ERROR);
                span.finish();
                Sentry.captureException(e);
            }
        } finally {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });

        }
    }

    private void processProductsInfo() {
        OkHttpClient client = new RequestClient().getNp1Client();
        // Docs say we need at least 10 calls to get a n+1 api issue
        for (int i = 0; i < 20; i++) {
            final int index = i;
            new Thread(() -> {
                Request request = new Request.Builder()
                        .url(getProductInfoUrl(index))
                        .build();
                try {
                    client.newCall(request).execute().close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }

    private void processProducts() {
        getIterator(44);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private int getIterator(int n) {
        if (n <= 0) {
            return 0;
        }
        if (n == 1 || n == 2) {
            return 1;
        }
        return getIterator(n-1) + getIterator(n-2);
    }

    public int getNumber() {
        int result = getAnotherNumber();
        return result;
    }

    public int getAnotherNumber() {
        return 2;
    }

    private String getEmpowerPlantDomain() {
        String domain = null;
        try {
            final ApplicationInfo appInfo = getActivity().getApplicationContext().getPackageManager().getApplicationInfo(getActivity().getApplicationContext().getPackageName(),
                    PackageManager.GET_META_DATA);

            if (appInfo.metaData != null) {
                domain = (String) appInfo.metaData.get("empowerplant.domain");
            }
        } catch (Exception e) {
            Sentry.captureException(e);
        }
        return domain;
    }

    private String getProductInfoUrl(int index) {
        String domain = this.getEmpowerPlantDomain();
        return domain + this.END_POINT_PRODUCT_INFO + "?id=" + index;
    }

    public void checkout() {
        Log.i("checkout", "checkout >>>");
        selectedStoreItems = this.adapter.getSelectedStoreItems();
        ITransaction checkoutTransaction = Sentry.startTransaction("checkout [android]", "http.client");
        checkoutTransaction.setOperation("http");
        Sentry.configureScope(scope -> scope.setTransaction(checkoutTransaction));

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
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

        String domain = this.getEmpowerPlantDomain();
        String checkoutURL = domain + this.END_POINT_CHECKOUT;

        RequestBody body = RequestBody.create(object.toString(), JSON);

        Log.d("checkout", "checkoutURL: " + checkoutURL);
        Log.d("checkout", "JSON of selectedStoreItems: " + object.toString());

        Request request = new Request.Builder()
                .url(checkoutURL)
                .header("email", "someone@gmail.com")
                .post(body)
                .build();

        OkHttpClient client = new RequestClient().getClient();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                progressDialog.dismiss();
                boolean success = response.isSuccessful();
                response.close();
                if (!success) {
                    Log.d("checkout", "response failed");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();

                            processDeliveryItem(checkoutTransaction);

                            checkoutTransaction.finish(SpanStatus.INTERNAL_ERROR);
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                progressDialog.dismiss();
                Sentry.captureException(e);

                processDeliveryItem(checkoutTransaction);
                checkoutTransaction.finish(SpanStatus.INTERNAL_ERROR);
                Log.e("checkout", "checkout failed");
            }
        });
        Log.i("checkout", "<<< checkout");
    }

    private JSONObject buildJSONPostData(HashMap<String, StoreItem> selectedStoreItems) {
        JSONObject jsonObject, postBody = new JSONObject();
        JSONObject cart = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        JSONObject quantities = new JSONObject();

        try {
            for (StoreItem s : selectedStoreItems.values()) {
                jsonObject = new JSONObject();

                jsonObject.put("name", s.getName());
                jsonObject.put("price", s.getPrice());
                jsonObject.put("image", s.getImage());
                jsonObject.put("id", s.getItemId());

                jsonArray.put(jsonObject);
                quantities.put(String.valueOf(s.getItemId()), s.getQuantity());

            }
            cart.put("items", jsonArray);
            cart.put("quantities", quantities);
            postBody.put("cart", cart);
            postBody.put("form", new JSONObject());// This line currently mocks non existent form data

        } catch (JSONException e) {
            ISpan span = Sentry.getSpan();
            if (span != null) {
                span.setThrowable(e);
                span.finish(SpanStatus.INTERNAL_ERROR);
                span.finish();
            }
            Sentry.captureException(e);
        }
        return postBody;
    }

    private void processDeliveryItem(ITransaction checkoutTransaction) {
        Log.i("processDeliveryItem", "processDeliveryItem >>>");
        ISpan processDeliverySpan = checkoutTransaction.startChild("task", "process delivery");

        try {
            Log.e("processDeliveryItem", "ERROR");
            throw new MainFragment.BackendAPIException("Failed to init delivery workflow");
        } catch (Exception e) {
            processDeliverySpan.setThrowable(e);
            processDeliverySpan.setStatus(SpanStatus.INTERNAL_ERROR);
            Sentry.captureException(e);
        }

        if (processDeliverySpan.getStatus() != SpanStatus.INTERNAL_ERROR) {
            processDeliverySpan.setStatus(SpanStatus.OK);
        }
        processDeliverySpan.finish();
        Log.i("processDeliveryItem", "<<< processDeliveryItem");
    }

    class BackendAPIException extends RuntimeException {

        public BackendAPIException(String message) {
            super(message);
        }
    }

    protected Boolean addAttachment() {
        File f = null;
        try {
            Context c = getActivity().getApplicationContext();
            File cacheDirectory = c.getCacheDir();
            f = File.createTempFile("tmp", ".txt", cacheDirectory);
            System.out.println("File path: " + f.getAbsolutePath());
            f.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write("test".getBytes(UTF_8));
            }
            String dateStr = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());

            Attachment attachment1 = new Attachment(f.getAbsolutePath(), "tmp_" + dateStr + ".txt", "text/plain");

            Sentry.configureScope(
                    scope -> {
                        String json = "{ \"number\": 10 }";
                        Attachment attachment2 = new Attachment(json.getBytes(), "log_" + dateStr + ".json", "text/plain");
                        scope.addAttachment(attachment1);
                        scope.addAttachment(attachment2);
                    });
        } catch (Exception e) {
            Sentry.captureException(e);
            e.printStackTrace();
        }
        return true;
    }

    public void insertMultipleStoreItems() {

        AppDatabase.getInstance(getActivity().getApplicationContext())
                .StoreItemDAO().insertAll(empowerStoreItems);
    }

}