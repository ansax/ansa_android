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
import android.util.Log;
import android.view.View;
import android.widget.EditText;

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

import java.util.HashMap;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

public class RegisterActivity extends AppCompatActivity {

    EditText mUsernameEditText, mPhoneEditText, mPasswordEditText, mConfirmPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mUsernameEditText = (EditText) findViewById(R.id.username_edit_text);
        mPhoneEditText = (EditText) findViewById(R.id.phone_edit_text);
        mPasswordEditText = (EditText) findViewById(R.id.password_edit_text);
        mConfirmPasswordEditText = (EditText) findViewById(R.id.confirm_password_edit_text);

        findViewById(R.id.toolbar_relative_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        findViewById(R.id.register_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsernameEditText.getText().toString().trim();
                String phoneNumber = mPhoneEditText.getText().toString().trim();
                String password = mPasswordEditText.getText().toString().trim();
                String confirmPassword = mConfirmPasswordEditText.getText().toString().trim();

                if (username.isEmpty()) {
                    mUsernameEditText.setError("Enter your full name");
                    return;
                }

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

                if (!confirmPassword.equals(password)){
                    mConfirmPasswordEditText.setError("Passwords do not match");
                    return;
                }

                register(phoneNumber, password, username);

            }
        });

    }

    public void register(String phone, String password, String username) {

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

        String apiUrl = "https://ansax.herokuapp.com/register";

        RequestQueue mRequestQueue;

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);

        // Setup the network to use the HTTPURLConnection client
        final Network network = new BasicNetwork(new HurlStack());

        // Instantiate the request queue
        mRequestQueue = new RequestQueue(cache, network);

        // Start the queue
        mRequestQueue.start();

        HashMap<String, String> parametersObject = new HashMap<String, String>();
        parametersObject.put("phone", phone);
        parametersObject.put("password", password);
        parametersObject.put("username", username);


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

                                startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                            } else {

                                mUsernameEditText.setText(null);
                                mPhoneEditText.setText(null);
                                mPasswordEditText.setText(null);
                                mConfirmPasswordEditText.setText(null);

                                new AlertDialog.Builder(RegisterActivity.this).
                                        setMessage("You have an account. Please log in").
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
                                new AlertDialog.Builder(RegisterActivity.this).
                                        setMessage("Please try again").
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

}
