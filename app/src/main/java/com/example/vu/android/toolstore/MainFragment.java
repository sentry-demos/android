package com.example.vu.android.toolstore;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.sentry.ISpan;
import io.sentry.Sentry;
import io.sentry.SentryTraceHeader;
import io.sentry.SpanStatus;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment implements StoreItemAdapter.ItemClickListener {

    protected StoreItemAdapter adapter;
    protected List<StoreItem> toolStoreItems = new ArrayList<StoreItem>();
    //private ArrayList<DataModel> list = new ArrayList<>();
    TextView textCartItemCount;
    int mCartItemCount = -1;
    public MainFragment() {
        // Required empty public constructor
        this.fetchToolsFromServer();
        adapter = new StoreItemAdapter(this);

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
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

        buildListData();
        initRecyclerView(view);
        return view;
    }

    private void initRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(layoutManager);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(list, this);
        recyclerView.setAdapter(adapter);
    }

//    private void buildListData() {
//        list.add(new DataModel("Avengers"));
//        list.add(new DataModel("Black Panther"));
//        list.add(new DataModel("Titanic"));
//        list.add(new DataModel("The Lion King"));
//        list.add(new DataModel("Star Wars"));
//        list.add(new DataModel("Jurassic World"));
//    }

    public void fetchToolsFromServer() {
        progressDialog = new ProgressDialog(this);
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
                    ToolStoreActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            httpSpan.finish(SpanStatus.OK);

                            if (responseStr != null && !responseStr.equals("")) {
                                ToolStoreActivity.this.runOnUiThread(new Runnable() {
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
    public void onItemClick(DataModel dataModel) {
        Fragment fragment = DetailFragment.newInstance(dataModel.getTitle());


        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        // transaction.replace(R.id.frame_container, fragment, "detail_fragment");

        transaction.hide(getActivity().getSupportFragmentManager().findFragmentByTag("main_fragment"));
        transaction.add(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onItemClick(StoreItem storeItem) {
        StoreItem selectedItem = list.get(holder.getAdapterPosition());
        selectedStoreItems.add(selectedItem);

        MainFragment.setBadgeNumber();
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



}