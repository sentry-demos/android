package com.example.vu.android.toolstore;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.sentry.ISpan;
import io.sentry.ITransaction;
import io.sentry.Sentry;
import io.sentry.SentryTraceHeader;
import io.sentry.SpanStatus;
import io.sentry.UserFeedback;
import io.sentry.protocol.SentryId;

public class StoreItemAdapter extends RecyclerView.Adapter<StoreItemAdapter.ViewHolder> {

    private Context context;
    private List<StoreItem> list;
    private List<StoreItem> selectedStoreItems;
    private ToolStoreActivity toolStoreActivity;

    public StoreItemAdapter(ToolStoreActivity toolStoreActivity) {
        this.toolStoreActivity = toolStoreActivity;
        this.context = toolStoreActivity.getApplicationContext();
        this.list = toolStoreActivity.toolStoreItems;
        selectedStoreItems = new ArrayList<StoreItem>();
    }

    public List<StoreItem> getSelectedStoreItems() {
        return selectedStoreItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.single_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        StoreItem storeItem = list.get(position);

        holder.textName.setText( storeItem.getName());
        holder.textSKU.setText("SKU: " + String.valueOf(storeItem.getSku()));
        holder.textPrice.setText("Price: " + String.valueOf(storeItem.getPrice()) + "$");
        holder.imageItem.setImageResource(this.getDrawable(String.valueOf(storeItem.getImage())));

        holder.addToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v ) {
                StoreItem selectedItem = list.get(holder.getAdapterPosition());
                selectedStoreItems.add(selectedItem);

                toolStoreActivity.setBadgeNumber();
            }
        });
    }

    private int getDrawable(String path){
        int s =  R.drawable.nails;
        if("hammer.png".equals(path)){
            s = R.drawable.hammer;
        }else if("wrench.png".equals(path)){
            s = R.drawable.wrench;
        }
        return s;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textName, textSKU, textPrice;
        public ImageView imageItem;
        public Button addToCartBtn;

        public ViewHolder(View itemView) {
            super(itemView);

            textName = itemView.findViewById(R.id.main_name);
            textSKU = itemView.findViewById(R.id.main_sku);
            textPrice = itemView.findViewById(R.id.main_price);
            imageItem = itemView.findViewById(R.id.main_image);
            addToCartBtn = itemView.findViewById(R.id.add_to_cart_btn);
        }
    }

}
