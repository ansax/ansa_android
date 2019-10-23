package com.ansa;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

public class NewAdActivity extends AppCompatActivity {

    EditText mAdMessageEditText;
    EditText mAdPriceEditText;
    EditText mAdUnitsEditText;
    String adMessage;
    String adPrice;
    String adUnits;
    private static final int SECOND_ACTIVITY_REQUEST_CODE = 2019;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_ad);

        mAdMessageEditText = (EditText) findViewById(R.id.new_ad_edit_text);
        mAdPriceEditText = (EditText) findViewById(R.id.price_edit_text);
        mAdUnitsEditText = (EditText) findViewById(R.id.units_edit_text);

        mAdPriceEditText.setTransformationMethod(new NumericKeyBoardTransformationMethod());


        findViewById(R.id.toolbar_relative_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NewAdActivity.this, HomeActivity.class));
            }
        });

        findViewById(R.id.pin_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                adMessage = mAdMessageEditText.getText().toString().trim();
                if(adMessage.length() > 2){
                    formatAdMessage(adMessage);
                }else {
                    mAdMessageEditText.setError("Make ad more specific");
                }

            }
        });
    }

    private void formatAdMessage(String message){
        adPrice = mAdPriceEditText.getText().toString().trim();

        if (!adPrice.isEmpty()){
            NumberFormat numberFormat = new DecimalFormat("#,###,###,###,###,###,###");

            message = message + " at " + numberFormat.format(Long.parseLong(adPrice)) + " bob";
            adUnits = mAdUnitsEditText.getText().toString().trim();
            if (!adUnits.isEmpty()){
                if (adUnits.toLowerCase().contains("per")) {
                    message = message + " " + adUnits;
                }else {
                    message = message + " per " + adUnits;
                }
            }
        }

        final String finalMessage = message;
        new AlertDialog.Builder(NewAdActivity.this)
                .setTitle("Confirm your ad")
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        adMessage = finalMessage;
                        Intent intent = new Intent(NewAdActivity.this, LocationActivity.class);
                        startActivityForResult(intent, SECOND_ACTIVITY_REQUEST_CODE);
                    }}).setNegativeButton("FIX AD", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }}).create().show();

    }


    public void createAd(String message, String latitude, String longitude){
        final ACProgressFlower dialog = new ACProgressFlower.Builder(this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.BLACK)
                .fadeColor(Color.LTGRAY)
                .bgColor(Color.WHITE)
                .petalThickness(3)
                .petalAlpha(1f)
                .petalCount(9)
                .sizeRatio(.2f)
                .build();

        dialog.show();

        String apiUrl = "https://ansax.herokuapp.com/ads/";

        RequestQueue mRequestQueue;

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);

        // Setup the network to use the HTTPURLConnection client
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the request queue
        mRequestQueue = new RequestQueue(cache, network);

        // Start the queue
        mRequestQueue.start();

        HashMap userParameters = getSharedPrefs();
        userParameters.put("msg", message);
        userParameters.put("latitude", latitude);
        userParameters.put("longitude", longitude);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, apiUrl, new JSONObject(userParameters), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Load the initial JSON request;
                        dialog.cancel();
                        new AlertDialog.Builder(NewAdActivity.this).
                                setMessage("Your ad is now live").
                                setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        mAdMessageEditText.setText(null);
                                        startActivity(new Intent(NewAdActivity.this, HomeActivity.class));
                                        finish();
                                    }
                                }).create().show();
                    } },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                dialog.cancel();
                                new AlertDialog.Builder(NewAdActivity.this).
                                        setMessage("Switch on GPS and internet then try again").
                                        setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                return;
                                            }
                                        }).create().show();
                                error.printStackTrace();

                                VolleyLog.e("Error: ", error.toString());
                                VolleyLog.e("Error: ", error.getLocalizedMessage());
                            }
                        });

        // Add the request to the RequestQueue
        mRequestQueue.add(jsObjRequest);
    }

    public HashMap getSharedPrefs(){
        String MyPREFERENCES = "MyPrefs" ;
        String USER_ID = "idKey";
        String USER_PHONE = "phoneKey";
        String USER_NAME = "usernameKey";

        SharedPreferences sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        String name = sharedPreferences.getString(USER_NAME, "");
        String id = sharedPreferences.getString(USER_ID, "");
        String phone = sharedPreferences.getString(USER_PHONE, "");

        HashMap<String, String> userParameters = new HashMap<String, String>();
        userParameters.put("phone", phone);
        userParameters.put("user_id", id);
        userParameters.put("username", name);

        return userParameters;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check that it is the SecondActivity with an OK result
        if (requestCode == SECOND_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) { // Activity.RESULT_OK

                // get latlong  from Intent
                String userLatitude = String.valueOf(data.getDoubleExtra("userLatitude", 0.000));
                String userLongitude = String.valueOf(data.getDoubleExtra("userLongitude", 0.000));

                createAd(adMessage, userLatitude, userLongitude);

            }
        }
    }

    private class NumericKeyBoardTransformationMethod extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return source;
        }
    }

}
