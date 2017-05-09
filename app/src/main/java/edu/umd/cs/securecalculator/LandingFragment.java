package edu.umd.cs.securecalculator;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class LandingFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();
    private Context mActivity = null;
    LinearLayout okL, helpL, outofAppL, logoutL;
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
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflator, container, savedInstanceState);
        View view = inflator.inflate(R.layout.fragment_landing, container, false);

        okL = (LinearLayout) view.findViewById(R.id.ok_column);
        helpL = (LinearLayout) view.findViewById(R.id.help_column);
        outofAppL = (LinearLayout) view.findViewById(R.id.other_column);
        logoutL = (LinearLayout) view.findViewById(R.id.logged_out_column);

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
                        if(logoutL.getChildCount() > 1)
                            logoutL.removeViews(1, logoutL.getChildCount() - 1);

                        Log.d(TAG, "Num of students: " + dataSnapshot.getChildrenCount());
                        for (DataSnapshot post : dataSnapshot.getChildren()) {
                            final User s = post.getValue(User.class);

                            if (s != null) {
                                Log.d(TAG, "Processing " + s.getDirectoryID() + " with status " + s.getStatus()
                                        + " and " + s.getLog().size() + " entries in log");

                                final TextView currentUsername = new TextView(mActivity);
                                currentUsername.setText(s.getDirectoryID());

                                LinearLayout.LayoutParams currentUsernameParams = new LinearLayout.LayoutParams(
                                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                currentUsernameParams.gravity = Gravity.CENTER;
                                currentUsername.setLayoutParams(currentUsernameParams);

                                if (s.getStatus().equals(FireDatabaseConstants.OK_STATUS)) {//status is ok
                                    currentUsername.setTextColor(Color.GREEN);
                                    okL.addView(currentUsername);
                                } else if (s.getStatus().equals(FireDatabaseConstants.HELP_STATUS)) {//status is help
                                    currentUsername.setTextColor(Color.YELLOW);
                                    helpL.addView(currentUsername);
                                } else if (s.getStatus().equals(FireDatabaseConstants.DONE_STATUS)) {//status is logged out
                                    currentUsername.setTextColor(Color.GRAY);
                                    logoutL.addView(currentUsername);
                                } else {//status is other
                                    currentUsername.setTextColor(Color.RED);
                                    outofAppL.addView(currentUsername);
                                }
                                currentUsername.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {//this is an anonymous inner class
                                        if (s.getStatus().equals("HELP")) {
                                            dbInteraction.updateStatus(classID, currentUsername.getText().toString(), FireDatabaseConstants.OK_STATUS);
                                        }

                                        //Alert with Log
                                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.myDialog));

                                        alertDialog.setTitle("Student Activity Log");
                                        final LinearLayout layout = new LinearLayout(getActivity());
                                        layout.setOrientation(LinearLayout.VERTICAL);
                                        ArrayList<String> temp = s.getLog();
                                        TextView[] logs = new TextView[temp.size()];
                                        for (int i = 0; i < logs.length; i++) {
                                            logs[i] = new TextView(getActivity());
                                            logs[i].setText(temp.get(i));
                                            layout.addView(logs[i]);
                                        }
                                        final ScrollView scrollView = new ScrollView(getActivity());
                                        scrollView.addView(layout);
                                        alertDialog.setView(scrollView);

                                        alertDialog.setPositiveButton("Clear Log",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        ArrayList<String> temp = new ArrayList<String>();
                                                        temp.add(getCurrentTime() + " - Teacher cleared log");
                                                        dbInteraction.updateLog(classID, s.getDirectoryID(), temp);
                                                    }
                                                });

                                        alertDialog.setNegativeButton("OK",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.cancel();
                                                    }
                                                });

                                        alertDialog.show();

                                        //Toast.makeText(getActivity(), s.getStatus().toString(), Toast.LENGTH_LONG).show();
                                        /*new AlertDialog.Builder(mActivity).setTitle("Log").setMessage(s.getStatus().toString())
                                            .setNegativeButton("Close", null)
                                            .setPositiveButton("Change Status", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    s.setStatus("OK");
                                                }
                                            });*/
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

        inflater.inflate(R.menu.fragment_landing, menu);

        Log.d(TAG,"OnCreateOptionsMenu");
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

                                    dbInteraction.updateStatus(classID, s.getDirectoryID(), FireDatabaseConstants.DONE_STATUS);
                                    // dbInteraction.appendToLog(classID, s.getDirectoryID(), "Instructor has ended the class.");
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Well fuck.
                            }
                        });
                dbInteraction.setClassNotInSession(classID);
                Toast.makeText(getActivity().getApplicationContext(), classID + " is ending. Have a good day!", Toast.LENGTH_SHORT).show();
                //TODO download and parse all logs

                return true;
            case R.id.menu_item_add_auth_user:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.myDialog));

                alertDialog.setTitle("Add Authorized User");
                alertDialog.setMessage("Enter New User's Directory ID");
                final EditText input = new EditText(getActivity());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);

                alertDialog.setPositiveButton("Submit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String user = input.getText().toString();
                                Toast.makeText(getActivity(), "User Added", Toast.LENGTH_SHORT).show();
                                dbInteraction.addAuthUser(classID,user);
                            }
                        });

                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                alertDialog.show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
    }

    public static LandingFragment newInstance() {
        Bundle args = new Bundle();

        LandingFragment fragment = new LandingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    // Gets the current time and returns it in the format: hour:minute:second.millisecond
    private String getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        System.out.println(sdf.format(cal.getTime()).toString());
        return(sdf.format(cal.getTime()));
    }
}