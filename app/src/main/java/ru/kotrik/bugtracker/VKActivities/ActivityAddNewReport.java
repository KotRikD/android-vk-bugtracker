package ru.kotrik.bugtracker.VKActivities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.os.PersistableBundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.kotrik.bugtracker.Adapters.AddAttachAdapter;
import ru.kotrik.bugtracker.Adapters.TagNewAdapter;
import ru.kotrik.bugtracker.CustomClasses.RecyclerItemClickListener;
import ru.kotrik.bugtracker.Helpers.Async.CookedDocument;
import ru.kotrik.bugtracker.Helpers.Async.CookedPostDocument;
import ru.kotrik.bugtracker.Helpers.Callback;
import ru.kotrik.bugtracker.Helpers.Exceptions.AccessDeniedBtException;
import ru.kotrik.bugtracker.Helpers.Exceptions.NoInternetException;
import ru.kotrik.bugtracker.Helpers.Utils;
import ru.kotrik.bugtracker.Models.NewItem;
import ru.kotrik.bugtracker.R;

public class ActivityAddNewReport extends AppCompatActivity implements View.OnClickListener {

    RelativeLayout select, main;
    ProgressBar loading;

    RecyclerView products, platforms, tags, attaches;
    EditText name, description, fictive, waiting_r;

    Switch hideDocument, vulnarability;
    Spinner priority, types;
    Button btnadd;

    ArrayList<NewItem> lProducts, lPlatforms, lTags;

    AddAttachAdapter attachadapter;

    private String bt_report_hash;
    private int platform_type;
    private int product_id;

    private final Pattern dd_values = Pattern.compile("cur\\['btFormDDValues']=(.*);");
    private final Pattern bt_hash = Pattern.compile("BugTracker.saveNewBug.pbind\\('', '(.*)'\\);");
    private final Pattern bt_versions = Pattern.compile("cur.newBugProductDD = new Dropdown\\(ge\\('bt_form_product'\\), (.*),");

    private AppCompatActivity mAct;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_report);
        mAct = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setElevation(7);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0x4);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0x4);
            }
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        select = findViewById(R.id.rl_select);
        main = findViewById(R.id.rl_main);
        loading = findViewById(R.id.pb_loading);

        loading.setIndeterminate(true);
        select.setVisibility(View.INVISIBLE);
        main.setVisibility(View.INVISIBLE);

        name = findViewById(R.id.name_bug);
        description = findViewById(R.id.description_bug);
        fictive = findViewById(R.id.fictive_result_bug);
        waiting_r = findViewById(R.id.wait_result_bug);

        attachadapter = new AddAttachAdapter(this);
        attaches = findViewById(R.id.attachments);
        attaches.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        attaches.setAdapter(attachadapter);

        products = findViewById(R.id.bug_product);
        products.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        products.addOnItemTouchListener(new RecyclerItemClickListener(this, products, new RecyclerItemClickListener.OnItemClickListener() {
            @Override public void onItemClick(View view, int position) {
                for (NewItem item : lProducts) {
                    if(item.activated) {
                        item.activated = false;
                    }
                }
                lProducts.get(position).activated = true;
                product_id = lProducts.get(position).id;
                products.getAdapter().notifyDataSetChanged();
                new CookedDocument(getApplicationContext(), new mCallbackGet(mAct), "https://vk.com/al_bugtracker.php?act=add").execute();
            }

            @Override public void onLongItemClick(View view, int position) {
                // do whatever
            }
        }));

        platforms = findViewById(R.id.bug_platform);
        platforms.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        tags = findViewById(R.id.bug_tags);
        tags.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        priority = findViewById(R.id.bug_priority);
        types = findViewById(R.id.bug_type_problem);
        btnadd = findViewById(R.id.btn_add);
        hideDocument = findViewById(R.id.hide_documents);
        vulnarability = findViewById(R.id.vulnerability);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.priorities));
        adapter.setDropDownViewResource(R.layout.add_new_report_spinner);
        priority.setAdapter(adapter);
        priority.setSelection(2);

        ArrayAdapter<String> adapter_t = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.type_problem));
        types.setAdapter(adapter_t);
        types.setSelection(1);

        btnadd.setOnClickListener(this);

        if(savedInstanceState != null) {
            name.setText(savedInstanceState.getString("name"));
            description.setText(savedInstanceState.getString("descr"));
        }

        new CookedDocument(this, new mCallbackStart(), "https://vk.com/al_bugtracker.php?act=add").execute();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case 4:
                    recreate();
                    break;
            }
        } else {
            Toast.makeText(mAct, "Нету прав на чтение файлов", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_add:
                String platforms = "";
                String tags = "";

                for(NewItem item: lTags) {
                    if (item.activated) {
                        tags+=String.valueOf(item.id)+",";
                    }
                }
                for (NewItem item: lPlatforms) {
                    if (item.activated) {
                        platforms+=String.valueOf(item.id)+",";
                    }
                }
                if(platforms.isEmpty()||tags.isEmpty()||name.getText().toString().isEmpty()||description.getText().toString().isEmpty()
                   ||fictive.getText().toString().isEmpty()||waiting_r.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Одно из полей не заполнено!", Toast.LENGTH_SHORT).show();
                    break;
                }


                Map<String, String> data = new HashMap<>();
                data.put("act", "a_save");
                data.put("hash", bt_report_hash);
                data.put("product", String.valueOf(product_id));
                data.put("title", name.getText().toString());
                data.put("descr", description.getText().toString());
                data.put("state_supposed", waiting_r.getText().toString());
                data.put("state_actual", fictive.getText().toString());
                data.put("issue_type", String.valueOf(types.getSelectedItemPosition()+1));
                data.put("severity", String.valueOf(priority.getSelectedItemPosition()+1));
                data.put("tags", tags);
                data.put("vulnerability", ((vulnarability.isChecked())?"1":"0"));
                data.put("confidential", ((hideDocument.isChecked())?"1":"0"));
                data.put("comment", "");
                data.put("phone", "");
                data.put("region_id", "0");
                data.put("box", "0");
                data.put("user_devices", "");
                if(platform_type == 1){
                    data.put("platforms", platforms);
                    data.put("platforms_versions", "");
                } else if(platform_type == 2){
                    data.put("platforms_versions", platforms);
                    data.put("platforms", "");
                }

                if(attachadapter.getAttaches().size() >0){
                    int count = 0;
                    for(String attach: attachadapter.getAttaches()) {
                        data.put("attachs["+count+"]", attach);
                        count+=1;
                    }
                } else {
                    data.put("attachs", "");
                }
                new CookedPostDocument(this, new addCallback(this), "https://vk.com/bugtracker", data).execute();
                break;
        }
    }

    class mCallbackStart extends Callback {
        @Override
        public void onSuccess(Document d) {
            loading.setVisibility(View.INVISIBLE);
            select.setVisibility(View.VISIBLE);
            lProducts = new ArrayList<NewItem>();
            Matcher m = bt_versions.matcher(d.html());
            while (m.find()) {
                    try {
                    JSONArray jsarray = new JSONArray(m.group(1));
                    for(int i=0; i<jsarray.length(); i++) {
                        JSONArray array = (JSONArray)jsarray.get(i);
                        lProducts.add(new NewItem(array.getInt(0), array.getString(1), false));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Произошла ошибка при парсинге", Toast.LENGTH_SHORT).show();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                }
            }
            TagNewAdapter tna = new TagNewAdapter(lProducts);
            products.setAdapter(tna);

            Matcher m2 = bt_hash.matcher(d.html());
            while (m2.find()) {
                bt_report_hash = m2.group(1);
            }
        }

        @Override
        public void onError(Exception e) {
            super.onError(e);
        }
    }

    class mCallbackGet extends Callback {

        private ProgressDialog dialog;

        public mCallbackGet(AppCompatActivity activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        public void onPreExecute() {
            dialog.setIndeterminate(true);
            dialog.setMessage(getResources().getString(R.string.wait_plz));
            dialog.show();
        }

        @Override
        public void onError(Exception e) {
            super.onError(e);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            Looper.prepare();
            if(e instanceof AccessDeniedBtException) {
                Toast.makeText(getApplicationContext(), R.string.error_access_denied, Toast.LENGTH_SHORT).show();
            }
            else if(e instanceof NoInternetException) {
                Toast.makeText(getApplicationContext(), R.string.error_no_internet, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onSuccess(Document d) {
            super.onSuccess(d);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            main.setVisibility(View.VISIBLE);
            Matcher m = dd_values.matcher(d.html());
            while (m.find()) {
                try {
                    JSONObject jsonObj = new JSONObject(m.group(1));
                    if (lPlatforms == null && lTags  == null){
                        lPlatforms = new ArrayList<NewItem>();
                        lTags = new ArrayList<NewItem>();
                    } else {
                        lPlatforms.clear();
                        lTags.clear();
                    }
                        JSONObject data = (JSONObject)jsonObj.get(String.valueOf(product_id));

                        JSONArray tagsr = data.getJSONArray("tags");
                        for(int i=0; i<tagsr.length(); i++) {
                            JSONArray array = (JSONArray)tagsr.get(i);
                            lTags.add(new NewItem(array.getInt(0), array.getString(1), false));
                        }

                        JSONArray platformsr = data.getJSONArray("platforms");
                        if(platformsr.length() < 2) {
                            platformsr = data.getJSONArray("platforms_versions");
                            platform_type = 2;
                        } else {
                            platform_type = 1;
                        }
                        for(int i=0; i<platformsr.length(); i++) {
                            JSONArray array = (JSONArray)platformsr.get(i);
                            lPlatforms.add(new NewItem(array.getInt(0), array.getString(1), false));
                        }

                    if(platforms.getAdapter() != null) {
                        platforms.getAdapter().notifyDataSetChanged();
                    } else {
                        platforms.setAdapter(new TagNewAdapter(lPlatforms));
                    }
                    if(tags.getAdapter() != null) {
                        tags.getAdapter().notifyDataSetChanged();
                    } else {
                        tags.setAdapter(new TagNewAdapter(lTags));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Произошла ошибка при парсинге", Toast.LENGTH_SHORT).show();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                }
            }
        }
    }

    class addCallback extends Callback {

        private ProgressDialog dialog;

        public addCallback(AppCompatActivity activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        public void onPreExecute() {
            dialog.setIndeterminate(true);
            dialog.setMessage(getResources().getString(R.string.wait_plz));
            dialog.show();
        }

        @Override
        public void onError(Exception e) {
            super.onError(e);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            Looper.prepare();
            if(e instanceof AccessDeniedBtException) {
                Toast.makeText(getApplicationContext(), R.string.error_access_denied, Toast.LENGTH_SHORT).show();
            }
            else if(e instanceof NoInternetException) {
                Toast.makeText(getApplicationContext(), R.string.error_no_internet, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onSuccess(Document d) {
            super.onSuccess(d);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            Toast.makeText(getApplicationContext(), "Готово! Твой супер отчёт был добавлен!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        attachadapter.onActivityResult(requestCode, resultCode, data);
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
        outState.putString("name", name.getText().toString());
        outState.putString("descr", name.getText().toString());
    }
}
