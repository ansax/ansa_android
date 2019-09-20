package com.ansa;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class AllChatsActivity extends AppCompatActivity {



    private Socket mSocket;
    RecyclerView rv;

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
        setContentView(R.layout.activity_all_chats);

       /* rv=(RecyclerView)findViewById(R.id.rv);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        mSocket.on(String.valueOf(getSharedPrefs().get("phone")), onNewMessage);


        mSocket.connect();


        findViewById(R.id.text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                HashMap<String, String> message = new HashMap<String, String>();
                message.put("username", String.valueOf(getSharedPrefs().get("username")));
                message.put("to_phone", "x2");
                message.put("from_phone", String.valueOf(getSharedPrefs().get("phone")));
                message.put("message", "x4");


                mSocket.emit("send message", new JSONObject(message));
            }
        });*/

       findViewById(R.id.vibrate).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 300 milliseconds
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                   vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
               } else {
                   //deprecated in API 26
                   vibrator.vibrate(300);
               }
           }
       });


    }


    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            AllChatsActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    /*String username;
                    String message;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                        Log.d("xxxxxxxxx1", data.toString());
                    } catch (JSONException e) {
                        Log.d("xxxxxxxxx2", data.toString());
                        return;
                    }
*/
                    // add the message to view
                    try {
                       Log.d("xxxxxxxxx3", formatDate(data.getString( "created")));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private String formatDate(String date){
        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("h:mm aa");
        SimpleDateFormat simpleDayMonthFormat = new SimpleDateFormat("d MMM");
        String time = simpleTimeFormat.format(new Date(date));
        String dayMonth = simpleDayMonthFormat.format(new Date(date));
        return dayMonth + " at " + time;
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

//    private void initializeAdapter(){
//        RVAdapter adapter = new RVAdapter(adsSortedByDistance);
//        rv.setAdapter(adapter);
//    }

}
