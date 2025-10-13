package com.SIMATS.smartcity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.google.android.material.button.MaterialButton;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class actsubscribepage extends AppCompatActivity implements PurchasesUpdatedListener {

    private MaterialButton btnSubscribe;
    private MaterialButton btnSkipForNow;
    private BillingClient billingClient;
    private ProductDetails productDetails;

    private static final String TAG = "SubscriptionActivity";
    private static final String SUBSCRIPTION_SKU = "smartcity_premium_subscription";
    private static final String TEST_SUBSCRIPTION_SKU = "android.test.purchased"; // For testing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subscribepage);

        addDebugInformation();
        initializeViews();
        setupBillingClient();
        setupClickListeners();
    }

    private void addDebugInformation() {
        Log.d(TAG, "=== DEBUG INFORMATION ===");
        Log.d(TAG, "Package name: " + getPackageName());

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            long versionCode;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = packageInfo.getLongVersionCode();
            } else {
                // This is the deprecated way, but it's safe for older versions.
                versionCode = packageInfo.versionCode;
            }
            Log.d(TAG, "Version code: " + versionCode);
            Log.d(TAG, "Version name: " + packageInfo.versionName);
        } catch (Exception e) {
            Log.w(TAG, "Unable to get package info: " + e.getMessage());
        }

        Log.d(TAG, "Product ID: " + SUBSCRIPTION_SKU);
        Log.d(TAG, "Test Product ID: " + TEST_SUBSCRIPTION_SKU);
        Log.d(TAG, "=========================");
    }

    private void initializeViews() {
        btnSubscribe = findViewById(R.id.btnSubscribe);
        btnSkipForNow = findViewById(R.id.btnSkipForNow); // This line was missing
    }

    private void setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
                .setListener(this)
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing setup finished successfully");
                    querySubscriptionDetails();
                } else {
                    Log.e(TAG, "Billing setup failed: " + billingResult.getDebugMessage());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d(TAG, "Billing service disconnected");
            }
        });
    }

    private void querySubscriptionDetails() {
        querySpecificProduct(SUBSCRIPTION_SKU, BillingClient.ProductType.SUBS, success -> {
            if (!success) {
                Log.w(TAG, "Real subscription product not found, trying test products...");
                querySpecificProduct(TEST_SUBSCRIPTION_SKU, BillingClient.ProductType.INAPP, testSuccess -> {
                    if (!testSuccess) {
                        Log.e(TAG, "Both real and test products failed");
                        showNoProductsAvailable();
                    }
                });
            }
        });
    }

    private void querySpecificProduct(String productId, String productType, java.util.function.Consumer<Boolean> callback) {
        QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(productType)
                .build();

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(ImmutableList.of(product))
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, productDetailsList) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                if (productDetailsList != null && !productDetailsList.isEmpty()) {
                    productDetails = productDetailsList.get(0);
                    Log.d(TAG, "Product details retrieved successfully for: " + productId);
                    callback.accept(true);
                } else {
                    Log.e(TAG, "No product details found for: " + productId);
                    callback.accept(false);
                }
            } else {
                Log.e(TAG, "Failed to query product details for " + productId + ": " + billingResult.getDebugMessage());
                callback.accept(false);
            }
        });
    }

    private void showNoProductsAvailable() {
        runOnUiThread(() -> Toast.makeText(this, "No subscription products available. Check your setup in Play Console.", Toast.LENGTH_LONG).show());
    }

    private void setupClickListeners() {
        btnSkipForNow.setOnClickListener(v -> {
            // Navigate to actwelcomepage
            startActivity(new Intent(this, actwelcomepage.class));
            finish();
        });
        btnSubscribe.setOnClickListener(v -> launchSubscriptionFlow());
    }

    private void launchSubscriptionFlow() {
        if (!billingClient.isReady()) {
            Log.e(TAG, "Billing client is not ready");
            Toast.makeText(this, "Billing service not ready. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (productDetails != null) {
            ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList;

            if (BillingClient.ProductType.SUBS.equals(productDetails.getProductType())) {
                List<ProductDetails.SubscriptionOfferDetails> offerDetails = productDetails.getSubscriptionOfferDetails();
                if (offerDetails == null || offerDetails.isEmpty()) {
                    Log.e(TAG, "No subscription offers available");
                    Toast.makeText(this, "No subscription offers available", Toast.LENGTH_SHORT).show();
                    return;
                }
                String offerToken = offerDetails.get(0).getOfferToken();
                Log.d(TAG, "Using subscription offer token: " + offerToken);
                productDetailsParamsList = ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .setOfferToken(offerToken)
                                .build()
                );
            } else {
                Log.d(TAG, "Using in-app product: " + productDetails.getProductId());
                productDetailsParamsList = ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                );
            }

            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build();

            BillingResult billingResult = billingClient.launchBillingFlow(this, billingFlowParams);
            if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                Log.e(TAG, "Failed to launch billing flow: " + billingResult.getDebugMessage());
            }
        } else {
            Log.e(TAG, "No product details available");
            Toast.makeText(this, "Subscription not available. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled the purchase");
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            Log.d(TAG, "Item already owned");
            navigateToMain();
        } else {
            Log.e(TAG, "Purchase failed with code: " + billingResult.getResponseCode());
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Purchase acknowledged successfully");
                        onSubscriptionSuccess();
                    }
                });
            } else {
                onSubscriptionSuccess();
            }
        }
    }

    private void onSubscriptionSuccess() {
        Toast.makeText(this, "Subscription successful! Welcome to Premium!", Toast.LENGTH_LONG).show();
        SharedPreferences sharedPref = getSharedPreferences("subscription_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("is_premium_user", true);
        editor.putLong("subscription_time", System.currentTimeMillis());
        editor.apply();
        navigateToMain();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, actwelcomepage.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingClient != null) {
            billingClient.endConnection();
        }
    }
}


