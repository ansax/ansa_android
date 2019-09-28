package com.ansa;

import android.app.Application;
import com.android.volley.RequestQueue;
import com.ansa.MapRouteHelpers.VolleySingleton;

public class MyApplication extends Application{
    private RequestQueue requestQueue;
    @Override
    public void onCreate() {
        super.onCreate();
        requestQueue = VolleySingleton.getInstance(getApplicationContext()).getRequestQueue();
    }
    public RequestQueue getVolleyRequestQueue(){
        return requestQueue;
    }
}
