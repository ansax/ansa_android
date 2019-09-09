package com.ansa;

import java.util.Comparator;

public class Ad {
    String mUsername;
    String mDate;
    double mDistance;
    String mMessage;
    String mPhone;

    Ad(String name, String date, double distance, String phone, String message) {
        this.mUsername = name;
        this.mDate = date;
        this.mDistance = distance;
        this.mPhone = phone;
        this.mMessage = message;
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

    public static Comparator distanceComparator = new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
            Ad ad1 =((Ad) o1);
            Ad ad2 =((Ad) o2);
            return (Double.compare(ad2.getDistance(), ad1.getDistance()));
        }
    };

}