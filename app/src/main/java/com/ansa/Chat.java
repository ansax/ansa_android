package com.ansa;

import java.util.Comparator;

public class Chat {
    String mUsername;
    int mCounter;

    Chat(String name, int counter) {
        this.mUsername = name;
        this.mCounter = counter;
    }

    public String getUsername() {
        return mUsername;
    }

    public int getCounter() {
        return mCounter;
    }


}