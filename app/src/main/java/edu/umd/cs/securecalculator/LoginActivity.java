package edu.umd.cs.securecalculator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A login screen that offers login via email/classID.
 */
public class LoginActivity extends AppCompatActivity{
    private final String TAG = getClass().getSimpleName();

    // Id to identity READ_CONTACTS permission request.
    private static final int REQUEST_READ_CONTACTS = 0;

    // UI references.
    private EditText mDirectoryID;
    private EditText mClassIDView;
    private View mProgressView;
    private View mLoginFormView;

    // Firebase Database
    private FirebaseDatabase fireDB;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mDirectoryID = (EditText) findViewById(R.id.directoryID);
        mClassIDView = (EditText) findViewById(R.id.classID);
        mClassIDView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        // Init Firebase DB
        fireDB = FirebaseDatabase.getInstance();
        database = fireDB.getReference();
    }

    private boolean isDirectoryIDValid(String directoryID) {
        String regexStr = "^[0-9]*$";
        if(directoryID.trim().matches(regexStr)){
            return false;
        } else {
            return true;
        }
    }

    private boolean isClassIDValid(String classID) {
        return true;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mDirectoryID.setError(null);
        mClassIDView.setError(null);

        // Store values at the time of the login attempt.
        final String directoryID = mDirectoryID.getText().toString();
        final String classID = mClassIDView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid classID, if the user entered one.
        if (!TextUtils.isEmpty(classID) && !isClassIDValid(classID)) {
            mClassIDView.setError(getString(R.string.error_invalid_classID));
            focusView = mClassIDView;
            cancel = true;
        }

        // Check for a valid directoryID address.
        if (TextUtils.isEmpty(directoryID)) {
            mDirectoryID.setError(getString(R.string.error_field_required));
            focusView = mDirectoryID;
            cancel = true;
        } else if (!isDirectoryIDValid(directoryID)) {
            mDirectoryID.setError(getString(R.string.error_invalid_directoryID));
            focusView = mDirectoryID;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            database.child(FireDatabaseConstants.DB_CLASS_CHILD).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dbData) {
                    // Does this class exists yet?
                    if (dbData.hasChild(classID)){
                        // is the class in session
                        Log.d(TAG, classID + " was found in Firebase");
                        if((boolean) dbData.child(classID)
                                .child(FireDatabaseConstants.DB_META_CHILD)
                                .child(FireDatabaseConstants.META_SESSION).getValue()) {
                            Intent studentOrInstructor;

                            // class exists and is in session, so log in as user
                            Log.d(TAG, classID + " is in session.");
                            if (dbData.child(classID)
                                    .hasChild(directoryID)){
                                Log.d(TAG, directoryID + " exists in class " + classID + " as a student");
                                studentOrInstructor = new Intent(getApplicationContext(), Calculator.class);
                            } else if (dbData.child(classID)
                                    .child(FireDatabaseConstants.DB_META_CHILD)
                                    .child(FireDatabaseConstants.META_USER)
                                    .hasChild(directoryID)){
                                Log.d(TAG, directoryID + " exists in class " + classID + " as an instructor");
                                studentOrInstructor = new Intent(getApplicationContext(), LandingActivity.class);
                            } else {
                                Log.d(TAG, directoryID + " joined late.");
                                addNewUser(classID, directoryID, "User joined late");
                                studentOrInstructor = new Intent(getApplicationContext(), Calculator.class);
                            }

                            // Move to calculator, this is a student.
                            studentOrInstructor.putExtra(Calculator.DIRECTORY_ID_EXTRA, directoryID);
                            studentOrInstructor.putExtra(Calculator.CLASS_ID_EXTRA, classID);
                            startActivity(studentOrInstructor);
                        } else { // class is not in session
                            Log.d(TAG, classID + " is not currently in session. Denying user");
                            Toast.makeText(getApplicationContext(), classID + " is not in session.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // class does not exists yet, so this is probably an instructor
                        Log.d(TAG, "Creating new class " + classID);
                        dbInteraction.addNewClass(classID, directoryID);
                        Intent instructor = new Intent(getApplicationContext(), LandingActivity.class);
                        instructor.putExtra(Calculator.CLASS_ID_EXTRA, classID);
                        instructor.putExtra(Calculator.DIRECTORY_ID_EXTRA, classID);
                        startActivity(instructor);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "Database user look up cancelled");
                }
            });
        }
    }

    private void addNewUser(String classID, String directoryID, String initLog){
        // User Status
       dbInteraction.updateStatus(classID, directoryID, FireDatabaseConstants.OK_STATUS);

        // Initalize Log
        ArrayList<String> log = new ArrayList<String>();
        log.add(initLog);

        // Create log
        dbInteraction.updateLog(classID, directoryID, log);
    }
}

