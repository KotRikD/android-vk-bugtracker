package ru.kotrik.bugtracker.VKActivities;

import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import ru.kotrik.bugtracker.R;
import ru.kotrik.bugtracker.VKFragments.FragmentProfile;
import ru.kotrik.bugtracker.VKFragments.FragmentTracker;

public class ActivityFragmentWrapper extends AppCompatActivity {

    Fragment fr;
    int set;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_wrapper);

        set = getIntent().getIntExtra("set", 0);
        if(savedInstanceState != null) {
            set = savedInstanceState.getInt("set");
        }

        switch (set) {
            case 1:
                fr = new FragmentProfile();
                break;
            case 2:
                fr = new FragmentTracker();
                break;
            default:
                finish();
                break;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, fr);
        transaction.commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt("set", set);
    }
}
