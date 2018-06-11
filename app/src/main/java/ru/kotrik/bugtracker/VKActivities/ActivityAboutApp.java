package ru.kotrik.bugtracker.VKActivities;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import ru.kotrik.bugtracker.Adapters.AboutAppAdapter;
import ru.kotrik.bugtracker.CustomClasses.CustomItemDecoration;
import ru.kotrik.bugtracker.R;
import ru.kotrik.bugtracker.BuildConfig;


public class ActivityAboutApp extends AppCompatActivity {

    RecyclerView rv;
    TextView version;
    ImageView logo;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_app);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        rv = findViewById(R.id.items_rv);
        version = findViewById(R.id.version_name);
        logo = findViewById(R.id.iv);
  /*      logo.setOnTouchListener(new SwipeTouchListener(this){
            @Override
            public void onSwipeUp() {
                ObjectAnimator animator = ObjectAnimator.ofFloat(logo, "translationY", 0, -50, 0);
                animator.setInterpolator(new EasingInterpolator(Ease.LINEAR));
                animator.setDuration(1000);
                animator.start();
            }
        }); */


        version.setText("Версия "+BuildConfig.VERSION_NAME+" ("+BuildConfig.VERSION_CODE+")");

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new AboutAppAdapter(this));
        rv.addItemDecoration(new CustomItemDecoration(this, 2));

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
