package ru.kotrik.bugtracker;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class ImageViewer extends AppCompatActivity {

    String url;
    SubsamplingScaleImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        url = getIntent().getStringExtra("url");
        if (url == null) {finish();}

        if(savedInstanceState != null) {
            url = savedInstanceState.getString("url");
        }

        imageView = (SubsamplingScaleImageView)findViewById(R.id.imageView);

        new LoadImage().execute();
    }

    public class LoadImage extends AsyncTask<String, String, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap image = null;
            try {
                image = Picasso.with(getApplicationContext()).load(url).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return image;
        }

        @Override
        protected void onPostExecute(Bitmap s) {
            super.onPostExecute(s);
            try {
                imageView.setImage(ImageSource.bitmap(s));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Произошла ошибка", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
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

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putString("url", url);
    }
}
