package com.ansa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class AdSorter {
    ArrayList<Ad> adsList = new ArrayList<>();

    public AdSorter(ArrayList adList) {
        this.adsList = adList;
    }

    public ArrayList<Ad> getAdsSortedByDistance() {
        Collections.sort(adsList, Ad.distanceComparator);
        return adsList;
    }
}