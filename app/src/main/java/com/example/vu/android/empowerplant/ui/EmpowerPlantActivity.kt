package com.example.vu.android.empowerplant.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vu.android.BaseActivity
import com.example.vu.android.R
import com.example.vu.android.featurelist.FeatureListActivity
import kotlinx.coroutines.flow.collectLatest

class EmpowerPlantActivity : BaseActivity() {

    private lateinit var viewModel: MainViewModel

    private var cartItemCountTextView: TextView? = null
    private var loadingView: View? = null

    private var adapter: StoreItemAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empowerplant)
        loadingView = findViewById(R.id.loading)

        viewModel =
            ViewModelProvider(this)[MainViewModel::class.java]

        adapter = StoreItemAdapter { item ->
            viewModel.onAddToCartClicked(item)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context,
                layoutManager.orientation
            )
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter


        lifecycleScope.launchWhenStarted {
            viewModel.numberOfItemsInCart.collectLatest {
                cartItemCountTextView?.text = "$it"
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.storeItems.collectLatest {
                adapter?.setItems(it)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.isLoading.collectLatest { loading ->
                loadingView?.visibility = if (loading) View.VISIBLE else View.GONE
            }
        }

        // addAttachment(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.empowerplant_toplevel, menu)
        val menuItem = menu.findItem(R.id.action_cart)
        val actionView = menuItem.actionView
        cartItemCountTextView = actionView?.findViewById<View>(R.id.cart_badge) as TextView
        actionView.setOnClickListener { onOptionsItemSelected(menuItem) }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_cart -> {
            viewModel.onCheckoutClicked()
            true
        }

        R.id.action_open_listapp -> {
            val intent = Intent(this, FeatureListActivity::class.java)
            startActivity(intent)
            true
        }

        else -> super.onOptionsItemSelected(item)
    }
}