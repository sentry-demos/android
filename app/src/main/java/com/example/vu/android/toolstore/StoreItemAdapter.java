package com.example.vu.android.toolstore;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.vu.android.R;

import java.util.ArrayList;
import java.util.List;

public class StoreItemAdapter extends RecyclerView.Adapter<StoreItemAdapter.ViewHolder> {

    private List<StoreItem> list;
    private List<StoreItem> selectedStoreItems;
    private ItemClickListener clickListener;

    public StoreItemAdapter(List<StoreItem> list, ItemClickListener clickListener) {
        this.list = list;
        this.clickListener  = clickListener;
        selectedStoreItems = new ArrayList<StoreItem>();
    }

    public List<StoreItem> getSelectedStoreItems() {
        return selectedStoreItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_item, parent, false);
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
                clickListener.onItemClick(selectedItem);
            }
        });
    }

    private int getDrawable(String path){
        Log.d("PATH***",path);
        int s;
        if("https://storage.googleapis.com/application-monitoring/plant-spider-cropped.jpg".equals(path)){
            s = R.drawable.plantspider;
        }
        else if("https://storage.googleapis.com/application-monitoring/plant-to-text-cropped.jpg".equals(path)){
            s = R.drawable.moodplanter;
        }
        else if("https://storage.googleapis.com/application-monitoring/nodes-cropped.jpg".equals(path)){
            s = R.drawable.nodescropped;
        }else{
            s =  R.drawable.planttotext;
        }
        return s;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface ItemClickListener {

        public void onItemClick(StoreItem storeItem);
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