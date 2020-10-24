package com.msg91.sendotp.sample;

import android.app.Application;

import com.msg91.sendotpandroid.library.internal.SendOTP;


/**
 * Copyright (C) sendotp-android - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <p>
 * Created by Samset on 23,October,2020 at 6:34 PM for sendotp-android.
 * <p>
 * New Delhi,India
 */

public class AppController extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SendOTP.initializeApp(this,"64085ADihTNXf5dfa398aP1");
    }
}
