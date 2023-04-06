package com.example.vu.android.empowerplant.network

import com.example.vu.android.empowerplant.db.StoreItem
import io.sentry.Sentry
import io.sentry.SpanStatus
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

fun deserializeStoreItems(body: String): List<StoreItem> {
    val items = mutableListOf<StoreItem>()
    try {
        val jsonArray = JSONArray(body)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val storeItem = StoreItem(
                jsonObject.getString("id"),
                jsonObject.getString("title"),
                jsonObject.getString("imgcropped"),
                null,
                jsonObject.getInt("id"),
                jsonObject.getInt("price")
            )
            items.add(storeItem)
        }
    } catch (e: JSONException) {
        val span = Sentry.getSpan()
        if (span != null) {
            span.throwable = e
            span.status = SpanStatus.INTERNAL_ERROR
            span.finish()
            Sentry.captureException(e)
        }
    }
    return items
}

fun serializeCheckoutCart(selectedStoreItems: HashMap<StoreItem, Int>): JSONObject {
    var jsonObject: JSONObject
    val postBody = JSONObject()
    val cart = JSONObject()
    val jsonArray = JSONArray()
    val quantities = JSONObject()
    try {
        for (entry in selectedStoreItems.entries) {
            val item = entry.key
            val quantity = entry.value

            jsonObject = JSONObject()
            jsonObject.put("name", item.name)
            jsonObject.put("price", item.price)
            jsonObject.put("image", item.image)
            jsonObject.put("id", item.itemId)
            jsonArray.put(jsonObject)
            quantities.put(item.itemId.toString(), quantity)
        }
        cart.put("items", jsonArray)
        cart.put("quantities", quantities)
        postBody.put("cart", cart)
        postBody.put("form", JSONObject()) // This line currently mocks non existent form data
    } catch (e: JSONException) {
        val span = Sentry.getSpan()
        if (span != null) {
            span.throwable = e
            span.finish(SpanStatus.INTERNAL_ERROR)
            span.finish()
        }
        Sentry.captureException(e)
    }
    return postBody
}