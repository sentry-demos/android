package com.example.vu.android.empowerplant.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vu.android.empowerplant.db.AppDatabase
import com.example.vu.android.empowerplant.db.StoreItem
import com.example.vu.android.empowerplant.network.RequestClient
import com.example.vu.android.empowerplant.network.deserializeStoreItems
import com.example.vu.android.empowerplant.network.serializeCheckoutCart
import io.sentry.ITransaction
import io.sentry.Scope
import io.sentry.Sentry
import io.sentry.SpanStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class MainViewModel(private val app: Application) : AndroidViewModel(app) {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asSharedFlow()

    private val _storeItems = MutableStateFlow(emptyList<StoreItem>())
    val storeItems = _storeItems.asSharedFlow()

    private val _numberOfItemsInCart = MutableStateFlow(0)
    val numberOfItemsInCart = _numberOfItemsInCart.asSharedFlow()

    private val selectedStoreItems: HashMap<StoreItem, Int> = HashMap()

    fun onAddToCartClicked(item: StoreItem) {
        val currentQuantity = selectedStoreItems[item] ?: 0
        selectedStoreItems[item] = currentQuantity + 1

        _numberOfItemsInCart.value = selectedStoreItems.map { it.value }.sum()
    }

    init {
        val transaction = Sentry.getSpan()
        val domain = RequestClient.getEmpowerPlantBaseUrl(app)
        val getToolsURL = domain + END_POINT_PRODUCTS
        val request: Request = Request.Builder()
            .url(getToolsURL)
            .build()

        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            // first retrieve any cached data, if there's any then hide loading indicator
            val cachedItems = AppDatabase.getInstance(app).StoreItemDAO().all
            _isLoading.value = cachedItems.isEmpty()
            _storeItems.value = cachedItems

            // refresh data from backend
            RequestClient.get().newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        val responseStr = response.body!!.string()
                        val taskSpan = transaction?.startChild("task", "Process server response.")
                        val items = deserializeStoreItems(responseStr)
                        taskSpan?.finish(SpanStatus.OK)

                        val persistSpan =
                            transaction?.startChild("db.insertAll", "Persist Store Items")
                        AppDatabase.getInstance(app)
                            .StoreItemDAO()
                            .insertAll(items)
                        persistSpan?.finish()

                        _storeItems.value = items
                        // finish Empower txn manually
                        Sentry.getCurrentHub().span?.finish()
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    _isLoading.value = false
                    transaction!!.finish(SpanStatus.INTERNAL_ERROR)
                }
            })
        }
    }


    fun onCheckoutClicked() {
        val checkoutTransaction = Sentry.startTransaction("checkout [android]", "http.client")
        checkoutTransaction.operation = "http"
        Sentry.configureScope { scope: Scope -> scope.transaction = checkoutTransaction }

        val processDataSpan = checkoutTransaction.startChild("task", "process_cart_data")
        val bodyJson = serializeCheckoutCart(selectedStoreItems)
        try {
            Thread.sleep(500)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        processDataSpan.finish()
        val domain = RequestClient.getEmpowerPlantBaseUrl(app)
        val checkoutURL = domain + END_POINT_CHECKOUT
        val body: RequestBody = bodyJson.toString().toRequestBody(MEDIA_TYPE_JSON)

        val request: Request = Request.Builder()
            .url(checkoutURL)
            .header("email", "someone@gmail.com")
            .post(body)
            .build()

        _isLoading.value = true
        RequestClient.get().newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                _isLoading.value = false

                processDeliveryItem(checkoutTransaction)
                checkoutTransaction.finish(if (response.isSuccessful) SpanStatus.OK else SpanStatus.INTERNAL_ERROR)
            }

            override fun onFailure(call: Call, e: IOException) {
                _isLoading.value = false

                Sentry.captureException(e)
                processDeliveryItem(checkoutTransaction)
                checkoutTransaction.finish(SpanStatus.INTERNAL_ERROR)
            }
        })
    }

    private fun processDeliveryItem(checkoutTransaction: ITransaction) {
        val processDeliverySpan = checkoutTransaction.startChild("task", "process delivery")
        try {
            throw ItemDeliveryProcessException("Failed to init delivery workflow")
        } catch (e: Exception) {
            processDeliverySpan.throwable = e
            processDeliverySpan.status = SpanStatus.INTERNAL_ERROR
            Sentry.captureException(e)
        }
        if (processDeliverySpan.status != SpanStatus.INTERNAL_ERROR) {
            processDeliverySpan.status = SpanStatus.OK
        }
        processDeliverySpan.finish()
    }

    internal inner class ItemDeliveryProcessException(message: String?) : RuntimeException(message)

    companion object {

        private const val END_POINT_PRODUCTS = "/products"
        private const val END_POINT_CHECKOUT = "/checkout"

        private val MEDIA_TYPE_JSON: MediaType = "application/json; charset=utf-8".toMediaType()
    }

}