package edu.umd.cs.securecalculator;

import android.util.Log;

/**
 * Created by nickyhosamane on 5/6/17.
 */


public class User {

    private int status;
    private String username;
    private Log log;

    private User() {
        //do nothing
    }

    public User(int status, String username, Log log){
        this.status = status;
        this.username = username;
        this.log = log;

    }

    public int getStatus(){
        return this.status;

    }

    public String getUsername(){
        return this.username;
    }

    public Log getLog(){
        return this.log;
    }
}
