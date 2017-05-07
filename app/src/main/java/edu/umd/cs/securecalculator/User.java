package edu.umd.cs.securecalculator;

import java.util.ArrayList;

/**
 * Created by nickyhosamane on 5/6/17.
 */


public class User {

    private String status;
    private String directoryID;
    private ArrayList<String> log;

    private User() {
        //do nothing
    }

    public User(String directoryID, String status, ArrayList<String> log){
        this.directoryID = directoryID;
        this.status = status;
        this.log = log;
    }

    public String getStatus(){
        return this.status;

    }

    public String getDirectoryID(){
        return this.directoryID;
    }

    public ArrayList<String> getLog(){
        return this.log;
    }

    public void setDirectoryID(String directoryID){
        this.directoryID = directoryID;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public void setLog(ArrayList<String> log){
        this.log = log;
    }
}
