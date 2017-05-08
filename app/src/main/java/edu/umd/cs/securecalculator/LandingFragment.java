package edu.umd.cs.securecalculator;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LandingFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();
    LinearLayout okL, helpL, outofAppL, doneL;
    String classID, directoryID;

    private LoginActivity loginActivity;
    // Firebase Database
    private FirebaseDatabase fireDB;
    private DatabaseReference database;

    @Override
    public void onCreate(Bundle save){
        super.onCreate(save);
        setHasOptionsMenu(true);

        // Init Firebase DB
        fireDB = FirebaseDatabase.getInstance();
        database = fireDB.getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflator,ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflator, container, savedInstanceState);
        setHasOptionsMenu(true);
        View view = inflator.inflate(R.layout.fragment_landing, container,false);
        okL = (LinearLayout) view.findViewById(R.id.ok_column);
        helpL = (LinearLayout) view.findViewById(R.id.help_column);
        outofAppL = (LinearLayout) view.findViewById(R.id.other_column);
        doneL = (LinearLayout) view.findViewById(R.id.logged_out_column);

        Bundle args = getArguments();
        classID = args.getString(Calculator.CLASS_ID_EXTRA);
        directoryID = args.getString(Calculator.DIRECTORY_ID_EXTRA);

        //get all information
        database.child(FireDatabaseConstants.DB_CLASS_CHILD)
                .child(classID)
                .child(FireDatabaseConstants.DB_USER_CHILD)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "Clearing out all views.");
                        if(okL.getChildCount() > 1)
                            okL.removeViews(1, okL.getChildCount()-1);
                        if(helpL.getChildCount() > 1)
                            helpL.removeViews(1, helpL.getChildCount()-1);
                        if(outofAppL.getChildCount() > 1)
                            outofAppL.removeViews(1, outofAppL.getChildCount()-1);
                        if(doneL.getChildCount() > 1)
                            doneL.removeViews(1, doneL.getChildCount() - 1);

                        Log.d(TAG, "Num of students: " + dataSnapshot.getChildrenCount());
                        for (DataSnapshot post : dataSnapshot.getChildren()) {
                            final User s = post.getValue(User.class);
                            Log.d(TAG, "Processing " + s.getDirectoryID() + " with status " + s.getStatus()
                                        + " and " + s.getLog().size() + " entries in log");

                            TextView currentUsername = new TextView(getActivity());
                            currentUsername.setText(s.getDirectoryID());

                            LinearLayout.LayoutParams currentUsernameParams = new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            currentUsernameParams.gravity = Gravity.CENTER;
                            currentUsername.setLayoutParams(currentUsernameParams);

                            if(s.getStatus().equals(FireDatabaseConstants.OK_STATUS)){//status is ok
                                currentUsername.setTextColor(Color.GREEN);
                                currentUsername.setOnClickListener(new View.OnClickListener(){
                                    @Override
                                    public void onClick(View view){//this is an anonymous inner class
                                        Toast.makeText(getActivity(), s.getDirectoryID()+":"+s.getStatus().toString(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                                okL.addView(currentUsername);
                            } else if(s.getStatus().equals(FireDatabaseConstants.HELP_STATUS)){//status is help
                                helpL.addView(currentUsername);
                                currentUsername.setTextColor(Color.YELLOW);
                                currentUsername.setOnClickListener(new View.OnClickListener(){
                                    @Override
                                    public void onClick(View view){//this is an anonymous inner class
                                        Toast.makeText(getActivity(), "Resetting "+ s.getDirectoryID()+" to OKAY".toString(), Toast.LENGTH_SHORT).show();
                                        dbInteraction.updateStatus(classID, s.getDirectoryID(), FireDatabaseConstants.OK_STATUS);
                                    }
                                });
                            } else if(s.getStatus().equals(FireDatabaseConstants.DONE_STATUS)) {//status is logged out (SORRY BUT DONE IS GOOD)
                                doneL.addView(currentUsername);
                                currentUsername.setTextColor(Color.GRAY);
                                currentUsername.setOnClickListener(new View.OnClickListener(){
                                    @Override
                                    public void onClick(View view){//this is an anonymous inner class
                                        Toast.makeText(getActivity(), s.getDirectoryID()+":"+s.getStatus().toString(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {//status is other
                                outofAppL.addView(currentUsername);
                                currentUsername.setTextColor(Color.RED);
                                currentUsername.setOnClickListener(new View.OnClickListener(){
                                    @Override
                                    public void onClick(View view){//this is an anonymous inner class
                                        Toast.makeText(getActivity(), s.getDirectoryID()+":"+s.getStatus().toString(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Well fuck.
                    }
                });
        return view;
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        Log.d(TAG,"OnCreateOptionsMenu");
        inflater.inflate(R.menu.fragment_landing, menu);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_end_class:
                database.child(FireDatabaseConstants.DB_CLASS_CHILD)
                        .child(classID)
                        .child(FireDatabaseConstants.DB_USER_CHILD)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Log.d(TAG, "Num of students: " + dataSnapshot.getChildrenCount());
                                //setting all students' statuses to LOGGED_OUT
                                for (DataSnapshot post : dataSnapshot.getChildren()) {
                                    final User s = post.getValue(User.class);
                                    Log.d(TAG, "Processing " + s.getDirectoryID() + " with status " + s.getStatus()
                                            + " and " + s.getLog().size() + " entries in log");

                                    dbInteraction.updateStatus(classID,directoryID,FireDatabaseConstants.LOG_OUT_STATUS);
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Well fuck.
                            }
                        });
                dbInteraction.setClassNotInSession(classID);
                //TODO download and parse all logs
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static LandingFragment newInstance() {
        Bundle args = new Bundle();

        LandingFragment fragment = new LandingFragment();
        fragment.setArguments(args);
        return fragment;
    }
}