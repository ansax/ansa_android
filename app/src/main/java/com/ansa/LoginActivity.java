package com.ansa;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

public class LoginActivity extends AppCompatActivity {

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissions = new ArrayList<>();
    private static final int ALL_PERMISSIONS_RESULT = 1011;
    private ArrayList<String> permissionsRejected = new ArrayList<>();

    EditText mPhoneEditText, mPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mPhoneEditText = (EditText) findViewById(R.id.phone_edit_text);
        mPasswordEditText = (EditText) findViewById(R.id.password_edit_text);

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionsToRequest = permissionsToRequest(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0) {
                requestPermissions(permissionsToRequest.toArray(
                        new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            }
        }

        try {
            if (!((String) getSharedPrefs().get("phone")).isEmpty()){
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        findViewById(R.id.create_account_text_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        findViewById(R.id.login_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = mPhoneEditText.getText().toString().trim();
                String password = mPasswordEditText.getText().toString().trim();

                if (phoneNumber.isEmpty()) {
                    mPhoneEditText.setError("Enter phone number");
                    return;
                } else if (phoneNumber.length() < 10){
                    mPhoneEditText.setError("Enter valid phone number");
                    return;
                }

                if (password.isEmpty()){
                    mPasswordEditText.setError("Enter valid password");
                    return;
                }

                login(phoneNumber, password);

            }
        });

    }

    public void login(String phone, String password) {
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

        String apiUrl = "https://ansax.herokuapp.com/login";

        RequestQueue mRequestQueue;

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);

        // Setup the network to use the HTTPURLConnection client
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the request queue
        mRequestQueue = new RequestQueue(cache, network);

        // Start the queue
        mRequestQueue.start();

        HashMap<String, String> parametersObject = new HashMap<String, String>();
            parametersObject.put("phone", phone);
            parametersObject.put("password", password);


        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, apiUrl, new JSONObject(parametersObject), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Load the initial JSON request;
                        dialog.cancel();
                        try {

                            if (response.length() > 1){
                                int id = response.getInt("id");
                                String username = response.getString("username");
                                String phone = response.getString("phone");

                                putSharedPrefs(id, phone, username);

                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            } else {

                                mPhoneEditText.setText(null);
                                mPasswordEditText.setText(null);
                                new AlertDialog.Builder(LoginActivity.this).
                                        setMessage("Check phone number and password then try again").
                                        setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                return;
                                            }
                                        }).create().show();
                            }

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


    private ArrayList<String> permissionsToRequest(ArrayList<String> wantedPermissions) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : wantedPermissions) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case ALL_PERMISSIONS_RESULT:
                for (String perm : permissionsToRequest) {
                    if (!hasPermission(perm)) {
                        permissionsRejected.add(perm);
                    }
                }

                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            new AlertDialog.Builder(LoginActivity.this).
                                    setMessage("Please allow location services").
                                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.
                                                        toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    }).setCancelable(false).create().show();

                            return;
                        }
                    }
                } else {

                }

                break;
        }
    }

    public void putSharedPrefs(int id, String phone, String name){
        String MyPREFERENCES = "MyPrefs" ;
        String USER_ID = "idKey";
        String USER_PHONE = "phoneKey";
        String USER_NAME = "usernameKey";

        SharedPreferences sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(USER_ID, String.valueOf(id));
        editor.putString(USER_PHONE, phone);
        editor.putString(USER_NAME, name);
        editor.commit();
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
        userParameters.put("id", id);
        userParameters.put("username", name);

        return userParameters;
    }
}

