package edu.umd.cs.securecalculator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class LandingActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        Bundle data = new Bundle();
        Intent callingIntent = getIntent();
        String directoryID =  String.valueOf(callingIntent.getExtras().get(Calculator.DIRECTORY_ID_EXTRA));
        String classID = String.valueOf(callingIntent.getExtras().get(Calculator.CLASS_ID_EXTRA));

        data.putString(Calculator.CLASS_ID_EXTRA, classID);
        data.putString(Calculator.DIRECTORY_ID_EXTRA, directoryID);

        LandingFragment landingFrag = LandingFragment.newInstance();
        landingFrag.setArguments(data);

        return landingFrag;
    }


}
