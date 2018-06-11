package ru.kotrik.bugtracker.VKActivities;

import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;


import ru.kotrik.bugtracker.CustomClasses.BottomNavigationViewEx;
import ru.kotrik.bugtracker.CustomClasses.ViewPagerAdapter;
import ru.kotrik.bugtracker.R;
import ru.kotrik.bugtracker.VKFragments.FragmentMyCard;
import ru.kotrik.bugtracker.VKFragments.FragmentProducts;
import ru.kotrik.bugtracker.VKFragments.FragmentTracker;

public class ActivityTracker extends AppCompatActivity {

    ViewPager vp;

    MenuItem prevMenuItem;
    BottomNavigationViewEx bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracker);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        vp = findViewById(R.id.viewpager);
        vp.setOffscreenPageLimit(3);
        setupViewPager(vp);

        bottomNavigationView = (BottomNavigationViewEx)
                findViewById(R.id.bottom_navigation);
        bottomNavigationView.enableAnimation(false);
        bottomNavigationView.enableShiftingMode(false);
        bottomNavigationView.enableItemShiftingMode(true);

        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                }
                else
                {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }

                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                prevMenuItem = bottomNavigationView.getMenu().getItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_tracker:
                                vp.setCurrentItem(0);
                                break;
                            case R.id.action_products:
                                vp.setCurrentItem(1);
                                break;
                            case R.id.action_profile:
                                vp.setCurrentItem(2);
                                break;
                        }
                        return false;
                    }
                });
    }

    public void setupViewPager(ViewPager vp) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        FragmentTracker tracker = new FragmentTracker();
        FragmentProducts products = new FragmentProducts();
        FragmentMyCard profile = new FragmentMyCard();
        adapter.addFragment(tracker);
        adapter.addFragment(products);
        adapter.addFragment(profile);
        vp.setAdapter(adapter);
    }


}
