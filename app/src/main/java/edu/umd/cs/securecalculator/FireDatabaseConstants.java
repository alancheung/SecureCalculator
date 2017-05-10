package edu.umd.cs.securecalculator;

/**
 * Created by Alan Cheung on 5/6/2017.
 */

public class FireDatabaseConstants {
    public static final String DB_USER_CHILD = "students";
    public static final String DB_CLASS_CHILD = "classes";
    public static final String DB_META_CHILD = "metadata";

    public static final String USER_STATUS = "status";
    public static final String USER_LOG = "log";

    public static final String META_USER = "authorized_user"; // creator username
    public static final String META_SESSION = "isInSession"; // values = T/F

    public static final String OK_STATUS = "OK";
    public static final String HELP_STATUS = "HELP";
    public static final String LOG_OUT_STATUS = "LOGGED_OUT";
    public static final String DONE_STATUS = "DONE";
    public static final String NOT_OK_STATUS = "ISSUE";
}
