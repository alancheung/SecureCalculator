package edu.umd.cs.securecalculator;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
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
    LinearLayout okL, helpL, outofAppL;
    String classID, directoryID;

    private LoginActivity loginActivity;
    // Firebase Database
    private FirebaseDatabase fireDB;
    private DatabaseReference database;

    @Override
    public void onCreate(Bundle save){
        super.onCreate(save);

        // Init Firebase DB
        fireDB = FirebaseDatabase.getInstance();
        database = fireDB.getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflator,ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflator, container, savedInstanceState);
        View view = inflator.inflate(R.layout.fragment_landing, container,false);
        okL = (LinearLayout) view.findViewById(R.id.ok_column);
        helpL = (LinearLayout) view.findViewById(R.id.help_column);
        outofAppL = (LinearLayout) view.findViewById(R.id.other_column);

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

                        Log.d(TAG, "Num of students: " + dataSnapshot.getChildrenCount());
                        for (DataSnapshot post : dataSnapshot.getChildren()) {
                            final User s = post.getValue(User.class);
                            Log.d(TAG, "Processing " + s.getDirectoryID() + " with status " + s.getStatus()
                                        + " and " + s.getLog().size() + " entries in log");

                            TextView currentUsername = new TextView(getActivity());
                            currentUsername.setText(s.getDirectoryID() + ":" + s.getStatus());

                            if(s.getStatus().equals("OK")){//status is ok
                                currentUsername.setTextColor(Color.GREEN);
                                okL.addView(currentUsername);
                            } else if(s.getStatus().equals("HELP")){//status is help
                                currentUsername.setTextColor(Color.YELLOW);
                                helpL.addView(currentUsername);
                            } else {//status is other
                                currentUsername.setTextColor(Color.RED);
                                outofAppL.addView(currentUsername);
                            }
                            currentUsername.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view){//this is an anonymous inner class
                                    Toast.makeText(getActivity(), s.getLog().toString(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Well fuck.
                    }
                });
        return view;
    }
    public static LandingFragment newInstance() {
        Bundle args = new Bundle();

        LandingFragment fragment = new LandingFragment();
        fragment.setArguments(args);
        return fragment;
    }
}