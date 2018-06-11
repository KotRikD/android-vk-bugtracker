package ru.kotrik.bugtracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import ru.kotrik.bugtracker.VKActivities.ActivityAddNewReport;
import ru.kotrik.bugtracker.VKActivities.ActivityBug;
import ru.kotrik.bugtracker.VKActivities.ActivityFragmentWrapper;
import ru.kotrik.bugtracker.VKActivities.ActivityLogin;
import ru.kotrik.bugtracker.VKActivities.ActivityTracker;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fabric.with(this, new Crashlytics());

        Long current_time = System.currentTimeMillis()/1000;
        System.out.println(current_time);
        System.out.println(Authdata.getStamp(this));
        if(current_time > Authdata.getStamp(this)) {
            Intent i = new Intent(this, ActivityLogin.class);
            startActivityForResult(i, 1);
        } else {
            new check_test_pool_subscribe().execute();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data==null || resultCode != RESULT_OK){ return;}
        final String access_token = data.getStringExtra("token");
        final String user_id = data.getStringExtra("user_id");

        final String cookies = data.getStringExtra("cookies");
        final long stamp = System.currentTimeMillis()/1000 + 3600;


        if(Authdata.getTokenVk(this) == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.warning)
                    .setMessage(R.string.warning_2)
                    .setPositiveButton(R.string.ok_button,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                            new Authdata(getApplicationContext(), user_id, access_token, cookies, stamp);
                                            new check_test_pool_subscribe().execute();
                                            dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            new Authdata(getApplicationContext(), user_id, access_token, cookies, stamp);
            new check_test_pool_subscribe().execute();
        }
    }

    class check_test_pool_subscribe extends AsyncTask<String, Void, Document>  {

        @Override
        protected Document doInBackground(String... strings) {
            Document doc = null;
            try {
                doc = Jsoup.connect("https://api.vk.com/method/groups.get?v=5.74&access_token="+Authdata.getTokenVk(getApplicationContext())).ignoreContentType(true).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return doc;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        boolean result = false;
        @Override
        protected void onPostExecute(Document s) {
            super.onPostExecute(s);
            try {
                JSONObject reader = new JSONObject(s.text());
                JSONObject response  = reader.getJSONObject("response");
                JSONArray groups = response.optJSONArray("items");
                for(int i=0; i<groups.length(); i++) {
                    if((Integer)groups.get(i) == 134304772) {
                        result = true;
                        break;
                    }
                }
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, R.string.start_text_error, Toast.LENGTH_SHORT).show();
                finish_c();
            }

            if (result) {
                if(getCallingActivity() == null){
                    if(getIntent().getAction() == null || getIntent().getAction().equals(Intent.ACTION_MAIN)) {
                        Intent i = new Intent(getApplicationContext(), ActivityTracker.class);
                        startActivity(i);
                    }
                    else if (getIntent().getAction().equals(Intent.ACTION_VIEW)){
                        Uri data = getIntent().getData();
                        String act = data.getQueryParameter("act");
                        String id = data.getQueryParameter("id");
                        if (act == null) {
                            Intent i = new Intent(getApplicationContext(), ActivityTracker.class);
                            startActivity(i);
                            finish();
                        } else {
                            switch(act) {
                                case "show":
                                    Intent i = new Intent(getApplicationContext(), ActivityBug.class);
                                    i.putExtra("url", "/bugtracker?act=show&id=" + id);
                                    startActivity(i);
                                    break;
                                case "reporter":
                                    Intent i2 = new Intent(getApplicationContext(), ActivityFragmentWrapper.class);
                                    i2.putExtra("url", "https://vk.com/bugtracker?act=reporter&id=" + id);
                                    i2.putExtra("set", 1);
                                    startActivity(i2);
                                    break;
                                case "add":
                                    Intent i3 = new Intent(getApplicationContext(), ActivityAddNewReport.class);
                                    startActivity(i3);
                                    break;
                                default:
                                    Toast.makeText(getApplicationContext(), "Ссылка не поддерживается приложением", Toast.LENGTH_SHORT).show();
                                    break;
                            }

                        }
                    }
                }
                finish();
            } else {
                Toast.makeText(MainActivity.this, R.string.start_text_error, Toast.LENGTH_SHORT).show();
                finish_c();
            }

        }
    }

    public void finish_c() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        });
    }

}
