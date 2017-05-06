package edu.umd.cs.securecalculator;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by Alan Cheung on 5/6/2017.
 */

public class dbInteraction {
    private static FirebaseDatabase fireDB = FirebaseDatabase.getInstance();
    private static DatabaseReference database = fireDB.getReference();

    /**
     * THis function interacts with this app's Firebase database to update a student's status
     * @param classID - The class to look for a student
     * @param directoryID - The student to look for
     * @param status - The new status
     */
    static void updateStatus(final String classID, String directoryID, String status){
        database.child(FireDatabaseConstants.DB_CLASS_CHILD)
                .child(classID)
                .child(FireDatabaseConstants.DB_USER_CHILD)
                .child(directoryID)
                .child(FireDatabaseConstants.USER_STATUS).setValue(status);
    }

    static void updateLog(final String classID, String directoryID, ArrayList<String> log){
        database.child(FireDatabaseConstants.DB_CLASS_CHILD)
                .child(classID)
                .child(FireDatabaseConstants.DB_USER_CHILD)
                .child(directoryID)
                .child(FireDatabaseConstants.USER_LOG).setValue(log);
    }

    static void addNewClass(String classID, String directoryID){
        // Add in a class
        database.child(FireDatabaseConstants.DB_CLASS_CHILD).child(classID).setValue(true);
        // Add in metadata (user and isInSession
        database.child(FireDatabaseConstants.DB_CLASS_CHILD).child(classID)
                .child(FireDatabaseConstants.DB_META_CHILD)
                .child(FireDatabaseConstants.META_USER).child(directoryID).setValue(true);
        database.child(FireDatabaseConstants.DB_CLASS_CHILD).child(classID)
                .child(FireDatabaseConstants.DB_META_CHILD)
                .child(FireDatabaseConstants.META_SESSION).setValue(true);
    }


}
