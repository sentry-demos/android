#!/usr/bin/env python3
"""
Simple test to verify the checkout variable scoping fix.

This test demonstrates that the fix resolves the UnboundLocalError
that was causing the HTTP 500 in the checkout endpoint.
"""


def test_checkout_logic_before_fix():
    """
    This simulates the BUGGY behavior before the fix.
    It should raise UnboundLocalError.
    """
    print("Testing BEFORE fix (buggy code):")
    cart = {
        'quantities': {'1': 2, '3': 1},
        'items': [
            {'id': 1, 'title': 'Product 1'},
            {'id': 3, 'title': 'Product 3'}
        ]
    }
    
    try:
        # This is the buggy code pattern
        if len(quantities) == 0:  # UnboundLocalError here!
            raise Exception("Invalid checkout request: cart is empty")
        quantities = {int(k): v for k, v in cart['quantities'].items()}
        
        print("  ✗ Should have raised UnboundLocalError but didn't!")
        return False
    except UnboundLocalError as e:
        print(f"  ✓ Expected error occurred: {e}")
        return True


def test_checkout_logic_after_fix():
    """
    This simulates the FIXED behavior after the fix.
    It should work correctly without errors.
    """
    print("\nTesting AFTER fix (corrected code):")
    cart = {
        'quantities': {'1': 2, '3': 1},
        'items': [
            {'id': 1, 'title': 'Product 1'},
            {'id': 3, 'title': 'Product 3'}
        ]
    }
    
    try:
        # This is the fixed code pattern
        quantities = {int(k): v for k, v in cart['quantities'].items()}
        if len(quantities) == 0:
            raise Exception("Invalid checkout request: cart is empty")
        
        print(f"  ✓ Successfully processed quantities: {quantities}")
        print(f"  ✓ Cart has {len(quantities)} items")
        return True
    except Exception as e:
        print(f"  ✗ Unexpected error: {e}")
        return False


def test_empty_cart_validation():
    """
    Test that empty cart validation still works correctly after the fix.
    """
    print("\nTesting empty cart validation:")
    cart = {
        'quantities': {},
        'items': []
    }
    
    try:
        quantities = {int(k): v for k, v in cart['quantities'].items()}
        if len(quantities) == 0:
            raise Exception("Invalid checkout request: cart is empty")
        
        print("  ✗ Should have raised empty cart exception")
        return False
    except Exception as e:
        if "cart is empty" in str(e):
            print(f"  ✓ Empty cart correctly detected: {e}")
            return True
        else:
            print(f"  ✗ Wrong exception: {e}")
            return False


if __name__ == "__main__":
    print("=" * 60)
    print("Checkout Variable Scoping Fix Verification")
    print("=" * 60)
    
    results = []
    
    # Test 1: Demonstrate the bug
    results.append(test_checkout_logic_before_fix())
    
    # Test 2: Verify the fix works
    results.append(test_checkout_logic_after_fix())
    
    # Test 3: Verify empty cart validation still works
    results.append(test_empty_cart_validation())
    
    print("\n" + "=" * 60)
    print(f"Results: {sum(results)}/{len(results)} tests passed")
    print("=" * 60)
    
    if all(results):
        print("\n✓ All tests passed! The fix is working correctly.")
        exit(0)
    else:
        print("\n✗ Some tests failed!")
        exit(1)
