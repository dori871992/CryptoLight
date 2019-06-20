package com.dorianmusaj.pgplibrary;

import android.app.Application;
import android.util.Log;

import com.dorianmusaj.cryptolight.CryptoLight;

public class LibraryApp extends Application {

    public static String DEBUG_TAG= "DEBUG_CRYPTO_LIGHT";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(DEBUG_TAG, "Application starts...");
        CryptoLight.init(this);
    }
}
