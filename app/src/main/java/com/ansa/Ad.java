package com.ansa;

import com.google.android.gms.maps.model.LatLng;

import java.util.Comparator;

public class Ad {
    String mUsername;
    String mDate;
    double mDistance;
    String mMessage;
    String mPhone;
    LatLng userLatLng;
    LatLng adLatLng;

    Ad(String name, String date, double distance, String phone, String message, LatLng userLatLng, LatLng adLatLng) {
        this.mUsername = name;
        this.mDate = date;
        this.mDistance = distance;
        this.mPhone = phone;
        this.mMessage = message;
        this.userLatLng = userLatLng;
        this.adLatLng = adLatLng;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getDate() {
        return mDate;
    }

    public double getDistance() {
        return mDistance;
    }

    public String getPhone() {
        return mPhone;
    }

    public String getMessage() {
        return mMessage;
    }

    public LatLng getUserLatLng() {
        return userLatLng;
    }

    public LatLng getAdLatLng() {
        return adLatLng;
    }

    public static Comparator distanceComparator = new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
            Ad ad1 =((Ad) o1);
            Ad ad2 =((Ad) o2);
            return (Double.compare(ad1.getDistance(), ad2.getDistance()));
        }
    };

}