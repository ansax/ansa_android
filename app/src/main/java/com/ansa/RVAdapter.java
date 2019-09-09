package com.ansa;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PersonViewHolder> {

    public static class PersonViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        TextView userName;
        TextView date;
        TextView distance;
        TextView message;

        PersonViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            userName = (TextView)itemView.findViewById(R.id.user_name_text_view);
            distance = (TextView)itemView.findViewById(R.id.distance_text_view);
            date = (TextView) itemView.findViewById(R.id.date_text_view);
            message = (TextView) itemView.findViewById(R.id.msg_text_view);
        }
    }

    List<Ad> ads;

    RVAdapter(List<Ad> ads){
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
    public void onBindViewHolder(PersonViewHolder personViewHolder, int i) {
        personViewHolder.userName.setText(ads.get(i).getUsername());
        personViewHolder.distance.setText(formatDistance(ads.get(i).getDistance()));
        personViewHolder.date.setText(ads.get(i).getDate());
        personViewHolder.message.setText(ads.get(i).getMessage());
    }

    @Override
    public int getItemCount() {
        return ads.size();
    }

    public static String formatDistance(double distance) {
        double distanceInKm = distance/1000;
        //return !(distanceInKm >= 1) ? String.format("%.0f", distance) + " m" : String.format("%.1f", distanceInKm) + " km";
        return !(distanceInKm >= 1) ? decimalFormat(distance) + " m" : decimalFormat(distanceInKm) + " km";
    }

    public static String decimalFormat(double distance){
        DecimalFormat df = new DecimalFormat("#.#");
        return df.format(distance);
    }
}