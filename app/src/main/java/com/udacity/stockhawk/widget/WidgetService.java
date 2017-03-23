package com.udacity.stockhawk.widget;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

public class WidgetService extends IntentService {

    public WidgetService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
}
