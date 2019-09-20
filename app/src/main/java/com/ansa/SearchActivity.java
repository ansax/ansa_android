package com.ansa;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import com.android.volley.toolbox.NoCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

public class SearchActivity extends AppCompatActivity {

    EditText mSearchView;
    TextView mSearchTextView;
    private ArrayList<Ad> adsList;
    ArrayList adsSortedByDistance;
    private RecyclerView rv;
    private TextView mNoSearchResultTextView;
    private static final int SECOND_ACTIVITY_REQUEST_CODE = 2019;
    double userLatitude;
    double userLongitude;

    String searchKeyWords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSearchView = (EditText) findViewById(R.id.search_edit_text);
        mSearchTextView = (TextView) findViewById(R.id.search_text_view);

        rv=(RecyclerView)findViewById(R.id.rv);
        mNoSearchResultTextView=(TextView) findViewById(R.id.no_search_result_text_view);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        findViewById(R.id.back_arrow_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchActivity.this, HomeActivity.class));
            }
        });


        mSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                mNoSearchResultTextView.setVisibility(View.GONE);
                if (s.length() > 0) {
                    mSearchTextView.setVisibility(View.VISIBLE);
                    mSearchTextView.setClickable(false);
                    mSearchTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (s.length() > 2) {
                                search(String.valueOf(s));
                            } else {
                                rv.setVisibility(View.GONE);
                                mNoSearchResultTextView.setVisibility(View.VISIBLE);
                                mNoSearchResultTextView.setText("Search something more specific");
                            }
                        }
                    });
                }else {
                    mSearchTextView.setVisibility(View.INVISIBLE);
                    mSearchTextView.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSearchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH){
                    String searchTerms = mSearchView.getText().toString().trim();
                    if (searchTerms.length() > 2){
                       search(searchTerms);
                    } else {
                        rv.setVisibility(View.GONE);
                        mNoSearchResultTextView.setVisibility(View.VISIBLE);
                        mNoSearchResultTextView.setText("Search something more specific");
                    }

                }
                return false;
            }
        });

        mSearchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int Drawable_RIGHT = 2;

                if(event.getAction() == MotionEvent.ACTION_UP){
                    if (event.getRawX() >= (mSearchView.getRight() - mSearchView.getCompoundDrawables()[Drawable_RIGHT].getBounds().width())){
                        mSearchView.setText(null);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void search(String searchTerms){
        searchKeyWords = searchTerms;
        Intent intent = new Intent(this, LocationActivity.class);
        startActivityForResult(intent, SECOND_ACTIVITY_REQUEST_CODE);
    }

    private void loadAds(final String searchKeyWords) {
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

        String apiUrl = "https://ansax.herokuapp.com/search";

        RequestQueue mRequestQueue;

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);

        // Setup the network to use the HTTPURLConnection client
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the request queue
      //  mRequestQueue = new RequestQueue(new NoCache(), network);
        mRequestQueue = new RequestQueue(cache, network);

        // Start the queue
        mRequestQueue.start();

        HashMap<String, String> parametersObject = new HashMap<String, String>();
        parametersObject.put("search_terms", searchKeyWords);
        Log.d("xxxxx",  parametersObject.get("search_terms") + " :");

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, apiUrl, new JSONObject(parametersObject), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Load the initial JSON request
                        dialog.cancel();
                        Log.d("xxxxx",  response.toString());
                        adsList = new ArrayList<>();
                        try {
                            JSONArray ads = response.getJSONArray("search_results");
                            if (ads.length() == 0 ){
                                Log.d("xxxxx",  "no result");
                                rv.setVisibility(View.GONE);
                                mNoSearchResultTextView.setVisibility(View.VISIBLE);
                                mNoSearchResultTextView.setText(searchKeyWords + " is not available. Create an ad saying you want " + searchKeyWords);
                            } else {
                                for (int i = 0; i < ads.length(); i++) {
                                    rv.setVisibility(View.VISIBLE);
                                    double adLatitude = ads.getJSONObject(i).getDouble("latitude");
                                    double adLongitude = ads.getJSONObject(i).getDouble("longitude");

                                    String username = ads.getJSONObject(i).getString("username");
                                    String date = formatDate(ads.getJSONObject(i).getString("created"));
                                    double distance = calculateDistance(userLatitude, userLongitude
                                            , adLatitude, adLongitude);

                                    String phone = ads.getJSONObject(i).getString("phone");
                                    String message = ads.getJSONObject(i).getString("msg");
                                    adsList.add(new Ad(username, date, distance, phone, message));
                                }

                                AdSorter adSorter = new AdSorter(adsList);
                                adsSortedByDistance = adSorter.getAdsSortedByDistance();
                                initializeAdapter();

                            }

                        } catch (JSONException e) {
                            dialog.cancel();
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

                loadAds(searchKeyWords);

            }
        }
    }
}
