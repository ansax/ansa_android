package com.ansa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class HomeActivity extends AppCompatActivity {

    private ArrayList<Ad> adsList;
    ArrayList adsSortedByDistance;
    private RecyclerView rv;
    SwipeRefreshLayout swipeRefreshLayout;
    private static final int SECOND_ACTIVITY_REQUEST_CODE = 2019;
    double userLatitude;
    double userLongitude;

    private static Socket mSocket;

    {
        try {
            IO.Options options = new IO.Options();
            options.transports = new String[]{WebSocket.NAME};
            mSocket = IO.socket("https://ansax.herokuapp.com/", options);
        } catch (URISyntaxException e) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent intent = new Intent(this, LocationActivity.class);
        startActivityForResult(intent, SECOND_ACTIVITY_REQUEST_CODE);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadLatestAds();
            }
        });

        rv=(RecyclerView)findViewById(R.id.rv);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        rv.addOnItemTouchListener(
                new RecyclerViewItemClickListener(getBaseContext(), rv ,new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        // do whatever
                        String toPhone = adsList.get(position).getPhone();

                        if (!String.valueOf(getSharedPrefs().get("phone")).equals(toPhone)) {
                            createMessagePopUp(adsList.get(position).getUsername(), toPhone
                                    , adsList.get(position).getMessage(), adsList.get(position).getDate());
                        }
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                        String toPhone = adsList.get(position).getPhone();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("user_position", adsList.get(position).getUserLatLng());
                        bundle.putParcelable("ad_position", adsList.get(position).getAdLatLng());
                        if (!String.valueOf(getSharedPrefs().get("phone")).equals(toPhone)) {
                            bundle.putString("username", adsList.get(position).getUsername() + " on " +
                                    adsList.get(position).getDate());
                        }else {
                            bundle.putString("username", "You on " + adsList.get(position).getDate());
                        }
                        bundle.putString("message", adsList.get(position).getMessage());
                        Intent intent = new Intent(HomeActivity.this, MapsActivity.class);
                        intent.putExtra("bundle", bundle);
                        startActivity(intent);

                    }
                })
        );


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

        mSocket.on(String.valueOf(getSharedPrefs().get("phone")), onNewMessage);
        mSocket.connect();

    }

    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            if ( activeNetwork != null && activeNetwork.isConnected()) {
                loadLatestAds();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReceiver);
    }

    private void loadLatestAds() {
        swipeRefreshLayout.setRefreshing(true);

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
                        swipeRefreshLayout.setRefreshing(false);
                        adsList = new ArrayList<>();
                        try {
                            JSONArray ads = response.getJSONArray("ads");
                            for (int i = 0; i < ads.length(); i++){
                                double adLatitude = ads.getJSONObject(i).getDouble("latitude");
                                double adLongitude = ads.getJSONObject(i).getDouble("longitude");

                                String username = ads.getJSONObject(i).getString("username");
                                String date = formatDate(ads.getJSONObject(i).getString("created"));
                                double distance = calculateDistance(userLatitude, userLongitude
                                        , adLatitude, adLongitude);

                                String phone = ads.getJSONObject(i).getString("phone");
                                String message = ads.getJSONObject(i).getString("msg");
                                adsList.add(new Ad(username, date, distance, phone, message, new LatLng(userLatitude, userLongitude),
                                        new LatLng(adLatitude, adLongitude)));
                            }
                         //   dialog.cancel();
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
                                swipeRefreshLayout.setRefreshing(false);
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

    public void createMessagePopUp(final String fromName, final String toPhone, String message, String time){
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog, null);

        final EditText editText = (EditText) dialogView.findViewById(R.id.message_edit_text);
        TextView usernameTextView = (TextView) dialogView.findViewById(R.id.from_name_text_view);
        TextView messageTextView = (TextView) dialogView.findViewById(R.id.message_text_view);
        TextView timeTextView = (TextView) dialogView.findViewById(R.id.time_text_view);
        ImageView sendBtn = (ImageView) dialogView.findViewById(R.id.send_button);

        usernameTextView.setText(fromName);
        messageTextView.setText(message);
        timeTextView.setText(time);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(msg)) {
                    sendMessage(toPhone, msg);
                    vibrate();
                }
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                dialogBuilder.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }


    public void sendMessage(String toPhone, String msg){

        HashMap<String, String> message = new HashMap<String, String>();
        message.put("username", String.valueOf(getSharedPrefs().get("username")));
        message.put("to_phone", toPhone);
        message.put("from_phone", String.valueOf(getSharedPrefs().get("phone")));
        message.put("message", msg);


        mSocket.emit("send message", new JSONObject(message));
    }

    private void vibrate(){

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(300);
        }
    }

    private  void playMessageReceivedTone(){
        MediaPlayer mediaPlayer = MediaPlayer.create(HomeActivity.this, R.raw.received);
        mediaPlayer.start();
    }


    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            HomeActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    String fromPhone;
                    String time;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                        fromPhone = data.getString( "from_phone");
                        time = getTime(data.getString( "created"));
                        createMessagePopUp(username, fromPhone, message, time);
                        playMessageReceivedTone();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            });
        }
    };

    private String getTime(String date){
        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("h:mm aa");
        return simpleTimeFormat.format(new Date(date));
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

    public HashMap getSharedPrefs(){
        String MyPREFERENCES = "MyPrefs" ;
        String USER_PHONE = "phoneKey";
        String USER_NAME = "usernameKey";

        SharedPreferences sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        String name = sharedPreferences.getString(USER_NAME, "");
        String phone = sharedPreferences.getString(USER_PHONE, "");

        HashMap<String, String> userParameters = new HashMap<String, String>();
        userParameters.put("phone", phone);
        userParameters.put("username", name);

        return userParameters;
    }



}