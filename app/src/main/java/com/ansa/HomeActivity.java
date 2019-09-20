package com.ansa;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

public class HomeActivity extends AppCompatActivity {

    private ArrayList<Ad> adsList;
    ArrayList adsSortedByDistance;
    private RecyclerView rv;
    private static final int SECOND_ACTIVITY_REQUEST_CODE = 2019;
    double userLatitude;
    double userLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent intent = new Intent(this, LocationActivity.class);
        startActivityForResult(intent, SECOND_ACTIVITY_REQUEST_CODE);

        rv=(RecyclerView)findViewById(R.id.rv);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);


        findViewById(R.id.new_ad_text_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, NewAdActivity.class));
            }
        });

        findViewById(R.id.search_edit_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, SearchActivity.class));
            }
        });
         findViewById(R.id.chats_text_view).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(HomeActivity.this, AllChatsActivity.class));
                    }
                });

    }

    private void loadLatestAds() {
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

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, apiUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Load the initial JSON request
                        dialog.cancel();
                        adsList = new ArrayList<>();
                        try {
                            JSONArray ads = response.getJSONArray("ads");
                            Log.d("xxxxx", ads.toString());
                            for (int i = 0; i < ads.length(); i++){
                                double adLatitude = ads.getJSONObject(i).getDouble("latitude");
                                double adLongitude = ads.getJSONObject(i).getDouble("longitude");

                                String username = ads.getJSONObject(i).getString("username");
                                Log.d("xxxxxx", ads.getJSONObject(i).getString("created"));
                                String date = formatDate(ads.getJSONObject(i).getString("created"));
                                double distance = calculateDistance(userLatitude, userLongitude
                                        , adLatitude, adLongitude);

                                String phone = ads.getJSONObject(i).getString("phone");
                                String message = ads.getJSONObject(i).getString("msg");
                                adsList.add(new Ad(username, date, distance, phone, message));
                            }
                            dialog.cancel();
                            AdSorter adSorter = new AdSorter(adsList);
                            adsSortedByDistance = adSorter.getAdsSortedByDistance();
                            initializeAdapter();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                dialog.cancel();
                                error.printStackTrace();

                                VolleyLog.e("Error: ", error.toString());
                                VolleyLog.e("Error: ", error.getLocalizedMessage());
                            }
                        });

        // Add the request to the RequestQueue
        mRequestQueue.add(jsObjRequest);
    }

    private String formatDate(String date){
        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("h:mm aa");
        SimpleDateFormat simpleDayMonthFormat = new SimpleDateFormat("d MMM");
        String time = simpleTimeFormat.format(new Date(date));
        String dayMonth = simpleDayMonthFormat.format(new Date(date));
        return dayMonth + " at " + time;
    }

    public static double calculateDistance(double userCurrentLatitude, double userCurrentLongitude, double adLatitude, double adLongitude) {
        float[] result = new float[1];
        Location.distanceBetween(userCurrentLatitude, userCurrentLongitude, adLatitude, adLongitude, result);
        return result[0];
    }

    private void initializeAdapter(){
        RVAdapter adapter = new RVAdapter(adsSortedByDistance);
        rv.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check that it is the SecondActivity with an OK result
        if (requestCode == SECOND_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) { // Activity.RESULT_OK

                // get latlong  from Intent
                userLatitude = data.getDoubleExtra("userLatitude", 0.000);
                userLongitude = data.getDoubleExtra("userLongitude", 0.000);

                loadLatestAds();

            }
        }
    }
}