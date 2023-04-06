package com.example.vu.android.empowerplant.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vu.android.R
import com.example.vu.android.empowerplant.db.StoreItem

class StoreItemAdapter(
    private val clickListener: (item: StoreItem) -> Unit
) : RecyclerView.Adapter<StoreItemAdapter.ViewHolder>() {

    private var items: List<StoreItem> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.single_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val storeItem = items[position]
        holder.textName.text = storeItem.name
        holder.textSKU.text = "SKU: ${storeItem.sku}"
        holder.textPrice.text = "Price: $${storeItem.price}"
        holder.imageItem.setImageResource(getDrawable(storeItem.image ?: ""))

        holder.addToCartBtn.setOnClickListener {
            val selectedItem = items[holder.adapterPosition]
            clickListener(selectedItem)
        }
    }

    private fun getDrawable(path: String): Int = when (path) {
        "https://storage.googleapis.com/application-monitoring/plant-spider-cropped.jpg" -> {
            R.drawable.plantspider
        }

        "https://storage.googleapis.com/application-monitoring/plant-to-text-cropped.jpg" -> {
            R.drawable.moodplanter
        }

        "https://storage.googleapis.com/application-monitoring/nodes-cropped.jpg" -> {
            R.drawable.nodescropped
        }

        else -> {
            R.drawable.planttotext
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(items: List<StoreItem>) {
        this.items = items
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView
        val textSKU: TextView
        val textPrice: TextView
        val imageItem: ImageView
        val addToCartBtn: Button

        init {
            textName = itemView.findViewById(R.id.main_name)
            textSKU = itemView.findViewById(R.id.main_sku)
            textPrice = itemView.findViewById(R.id.main_price)
            imageItem = itemView.findViewById(R.id.main_image)
            addToCartBtn = itemView.findViewById(R.id.add_to_cart_btn)
        }
    }
}