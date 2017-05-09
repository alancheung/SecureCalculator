package edu.umd.cs.securecalculator;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Alan Cheung on 5/6/2017.
 */

public class dbInteraction {
    private static FirebaseDatabase fireDB = FirebaseDatabase.getInstance();
    private static DatabaseReference database = fireDB.getReference();
    private static final String TAG = "dbInteraction";

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

    //Append String to Log
    static void appendToLog (final String classID, final String directoryID, String update) {
        final String newUpdate = update;
        database.child(FireDatabaseConstants.DB_CLASS_CHILD)
                .child(classID)
                .child(FireDatabaseConstants.DB_USER_CHILD)
                .child(directoryID)
                .addListenerForSingleValueEvent(new ValueEventListener() { // Database crashed when it was addValueEventListener
                    @Override
                    public void onDataChange (DataSnapshot snapshot) {
                        final User s = snapshot.getValue(User.class);
                        ArrayList<String> temp = s.getLog();
                        temp.add(newUpdate);
                        dbInteraction.updateLog(classID,directoryID,temp);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Well fuck.
                    }
                });
    }

    static void addNewClass(String classID, String directoryID){
        // Add in a class
        database.child(FireDatabaseConstants.DB_CLASS_CHILD).child(classID).setValue(true);
        // Add in metadata (user and isInSession
        database.child(FireDatabaseConstants.DB_CLASS_CHILD).child(classID)
                .child(FireDatabaseConstants.DB_META_CHILD)
                .child(FireDatabaseConstants.META_USER).child(directoryID).setValue(true);
        setClassInSession(classID);
    }

    static void addNewStudent(String classID, User user){
        database.child(FireDatabaseConstants.DB_CLASS_CHILD)
                .child(classID)
                .child(FireDatabaseConstants.DB_USER_CHILD)
                .child(user.getDirectoryID())
                .setValue(user);
    }

    static void setClassInSession(String classID){
        database.child(FireDatabaseConstants.DB_CLASS_CHILD)
                .child(classID)
                .child(FireDatabaseConstants.DB_META_CHILD)
                .child(FireDatabaseConstants.META_SESSION).setValue(true);
    }

    static void setClassNotInSession(String classID){
        database.child(FireDatabaseConstants.DB_CLASS_CHILD)
                .child(classID)
                .child(FireDatabaseConstants.DB_META_CHILD)
                .child(FireDatabaseConstants.META_SESSION).setValue(false);
    }

    static boolean isAuthUser(String classID, String directoryID){
        database.child(FireDatabaseConstants.DB_CLASS_CHILD);
        return false;
    }

    static void addAuthUser(String classID, String directoryID){
        database.child(FireDatabaseConstants.DB_CLASS_CHILD)
                .child(classID)
                .child(FireDatabaseConstants.DB_META_CHILD)
                .child(FireDatabaseConstants.META_USER)
                .child(directoryID).push();
        database.child(FireDatabaseConstants.DB_CLASS_CHILD)
                .child(classID)
                .child(FireDatabaseConstants.DB_META_CHILD)
                .child(FireDatabaseConstants.META_USER)
                .child(directoryID).setValue(true);
    }


}
