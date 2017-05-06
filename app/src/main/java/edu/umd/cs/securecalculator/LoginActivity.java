package edu.umd.cs.securecalculator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
public class LoginActivity extends Activity{
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
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(classID) &&
                            dataSnapshot.child(classID).hasChild(directoryID)) {
                        Log.d(TAG, directoryID + " exists in " + classID);
                    } else {
                        // Add new user
                        addNewUser(classID, directoryID);
                        Log.d(TAG, "Registered user " + mDirectoryID + " with classID " + classID);
                        Toast.makeText(LoginActivity.this, "New user added to class " + classID, Toast.LENGTH_SHORT)
                                .show();
                    }
                    Intent toCalcActivity = new Intent(getApplicationContext(), Calculator.class);
                    toCalcActivity.putExtra(Calculator.DIRECTORY_ID_EXTRA, directoryID);
                    toCalcActivity.putExtra(Calculator.CLASS_ID_EXTRA, classID);
                    startActivity(toCalcActivity);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "Database user look up cancelled");
                }
            });
        }
    }

    private void addNewUser(String classID, String directoryID){
        // User Status
        database.child(FireDatabaseConstants.DB_CLASS_CHILD).child(classID).child(directoryID)
                .child(FireDatabaseConstants.USER_STATUS).setValue("OK");

        // Initalize Log
        ArrayList<String> log = new ArrayList<String>();
        log.add("User logged in.");

        // Create log
        database.child(FireDatabaseConstants.DB_CLASS_CHILD).child(classID).child(directoryID)
                .child(FireDatabaseConstants.USER_LOG).setValue(log);
    }

    private boolean isDirectoryIDValid(String directoryID) {
        return true;
    }

    private boolean isClassIDValid(String classID) {
        //TODO does this class exists yet?
        return true;
    }
}

