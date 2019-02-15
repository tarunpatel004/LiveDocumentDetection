package com.example.techflitter.docscanner;

import android.app.Application;

import net.doo.snap.ScanbotSDKInitializer;


public class DocScannerApplication extends Application {

    @Override
    public void onCreate() {
        new ScanbotSDKInitializer().initialize(this);
        super.onCreate();
    }
}
