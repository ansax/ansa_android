package com.ansa;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PersonViewHolder> {

    public static class PersonViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        TextView userName;
        TextView date;
        TextView distance;
        TextView message;
        ImageView chat;
        ImageView call;
        ImageView direction;


        PersonViewHolder(final View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            userName = (TextView)itemView.findViewById(R.id.user_name_text_view);
            distance = (TextView)itemView.findViewById(R.id.distance_text_view);
            date = (TextView) itemView.findViewById(R.id.date_text_view);
            message = (TextView) itemView.findViewById(R.id.msg_text_view);
            chat = (ImageView) itemView.findViewById(R.id.chat_image_view);
            call = (ImageView) itemView.findViewById(R.id.call_image_view);
            direction = (ImageView) itemView.findViewById(R.id.direction_image_view);
        }

    }

    static List<Ad> ads;
    private Context mContext;
    private static Socket mSocket;

    {
        try {
            IO.Options options = new IO.Options();
            options.transports = new String[]{WebSocket.NAME};
            mSocket = IO.socket("https://ansax.herokuapp.com/", options);
        } catch (URISyntaxException e) {}
    }

    RVAdapter(Context context, List<Ad> ads){
        this.mContext = context;
        this.ads = ads;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
        PersonViewHolder pvh = new PersonViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(PersonViewHolder personViewHolder, final int i) {
        personViewHolder.userName.setText(ads.get(i).getUsername());
        personViewHolder.distance.setText(formatDistance(ads.get(i).getDistance()));
        personViewHolder.date.setText(ads.get(i).getDate());
        personViewHolder.message.setText(ads.get(i).getMessage());

        personViewHolder.chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getSharedPrefs().get("phone").toString().isEmpty()){
                    new AlertDialog.Builder(mContext).
                            setMessage("Log in to text " + ads.get(i).getUsername()).
                            setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mContext.startActivity(new Intent(mContext, LoginActivity.class));
                                }
                            }).create().show();
                }else {
                   String toPhone = ads.get(i).getPhone();

                    if (!String.valueOf(getSharedPrefs().get("phone")).equals(toPhone)) {
                        createMessagePopUp(ads.get(i).getUsername(), toPhone
                                , ads.get(i).getMessage(), ads.get(i).getDate());
                    }
                }

            }
        });

        personViewHolder.call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getSharedPrefs().get("phone") != null) {
                    if (!getSharedPrefs().get("phone").equals(ads.get(i).getPhone())) {
                        mContext.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + ads.get(i).getPhone())));
                    }
                } else {
                    mContext.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + ads.get(i).getPhone())));
                }
            }
        });

        personViewHolder.direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("user_position", ads.get(i).getUserLatLng());
                bundle.putParcelable("ad_position", ads.get(i).getAdLatLng());
                bundle.putString("username", "Ad by " + ads.get(i).getUsername());
                bundle.putString("distance", "Ad is " + formatDistance(ads.get(i).getDistance()) + " away");
                bundle.putString("message", ads.get(i).getMessage());
                Intent intent = new Intent(mContext, MapsActivity.class);
                intent.putExtra("bundle", bundle);
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return ads.size();
    }

    public static String formatDistance(double distance) {
        double distanceInKm = distance/1000;
        if (distance < 1)
            return "Here";
        //return !(distanceInKm >= 1) ? String.format("%.0f", distance) + " m" : String.format("%.1f", distanceInKm) + " km";
        return !(distanceInKm >= 1) ? decimalFormat(distance) + " m" : decimalFormat(distanceInKm) + " km";
    }

    public static String decimalFormat(double distance){
        DecimalFormat df = new DecimalFormat("#.#");
        return df.format(distance);
    }

    public HashMap getSharedPrefs(){
        String MyPREFERENCES = "MyPrefs" ;
        String USER_PHONE = "phoneKey";
        String USER_NAME = "usernameKey";

        SharedPreferences sharedPreferences = mContext.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        String name = sharedPreferences.getString(USER_NAME, "");
        String phone = sharedPreferences.getString(USER_PHONE, "");

        HashMap<String, String> userParameters = new HashMap<String, String>();
        userParameters.put("phone", phone);
        userParameters.put("username", name);

        return userParameters;
    }

    public void createMessagePopUp(final String fromName, final String toPhone, String message, String time){
        final AlertDialog dialogBuilder = new AlertDialog.Builder(mContext).create();
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
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

        Log.d("xxxxxxx", new JSONObject(message).toString());

        mSocket.emit("send message", new JSONObject(message));
    }

    private void vibrate(){

        Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(300);
        }
    }

}