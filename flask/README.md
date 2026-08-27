# Flask Backend for Android Demo

This directory contains the Flask backend service that provides API endpoints for the Android Empower Plant demo application.

## Fixed Issue: Checkout Variable Scoping Error

### Problem
The `/checkout` endpoint had a variable scoping error where `quantities` was being accessed before it was defined:

```python
# BEFORE (buggy code)
if len(quantities) == 0:  # ❌ UnboundLocalError - quantities not yet defined
    raise Exception("Invalid checkout request: cart is empty")
quantities = {int(k): v for k, v in cart['quantities'].items()}
```

This caused:
1. `UnboundLocalError: local variable 'quantities' referenced before assignment`
2. HTTP 500 Internal Server Error response
3. Android app receiving failed checkout response
4. Android app's fallback `processDeliveryItem()` throwing `BackendAPIException`

### Solution
The fix reorders the code to define `quantities` before accessing it:

```python
# AFTER (fixed code)
quantities = {int(k): v for k, v in cart['quantities'].items()}
if len(quantities) == 0:  # ✅ quantities is now defined
    raise Exception("Invalid checkout request: cart is empty")
```

### Impact
- Checkout requests now process successfully without UnboundLocalError
- Android app receives proper 200 OK responses for valid checkouts
- No more spurious "Failed to init delivery workflow" exceptions

## API Endpoints

### POST /checkout
Processes cart checkout and validates inventory.

**Request Body:**
```json
{
  "cart": {
    "items": [...],
    "quantities": {"1": 2, "3": 1},
    "total": 150
  },
  "form": {},
  "validate_inventory": "true"
}
```

**Response:**
```json
{
  "status": "success"
}
```

## Running the Backend

This backend requires:
- Python 3.x with Flask
- PostgreSQL database
- Redis cache
- Environment variables configured (see application-monitoring repo)

For full setup instructions, see the [application-monitoring repository](https://github.com/sentry-demos/application-monitoring).
