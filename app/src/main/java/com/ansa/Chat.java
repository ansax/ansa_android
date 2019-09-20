package com.ansa;


public class Chat {
    String mUsername;
    String mFromPhone;
    String mTimeCreated;
    String mMessage;

    Chat(String name, String fromPhone, String message, String time) {
        this.mUsername = name;
        this.mFromPhone = fromPhone;
        this.mMessage = message;
        this.mTimeCreated = time;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getFromPhone() {
        return mFromPhone;
    }

    public String getTimeCreated() {
        return mTimeCreated;
    }

    public String getMessage() {
        return mMessage;
    }


}