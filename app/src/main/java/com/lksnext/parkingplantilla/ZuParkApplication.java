package com.lksnext.parkingplantilla;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class ZuParkApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
