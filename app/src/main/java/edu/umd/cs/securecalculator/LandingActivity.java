package edu.umd.cs.securecalculator;

import android.support.v4.app.Fragment;

public class LandingActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return LandingFragment.newInstance();
    }


}
