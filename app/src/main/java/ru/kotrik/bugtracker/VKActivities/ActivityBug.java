package ru.kotrik.bugtracker.VKActivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.liuguangqiang.swipeback.SwipeBackActivity;
import com.liuguangqiang.swipeback.SwipeBackLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import ru.kotrik.bugtracker.Adapters.BugExpandableAdapter;
import ru.kotrik.bugtracker.Authdata;
import ru.kotrik.bugtracker.Helpers.Async.CookedDocument;
import ru.kotrik.bugtracker.Helpers.Callback;
import ru.kotrik.bugtracker.Helpers.Exceptions.AccessDeniedBtException;
import ru.kotrik.bugtracker.Helpers.Exceptions.NoInternetException;
import ru.kotrik.bugtracker.ImageViewer;
import ru.kotrik.bugtracker.R;
import ru.kotrik.bugtracker.Helpers.Utils;

public class ActivityBug extends SwipeBackActivity implements View.OnClickListener {

    TextView author, time, name, comment;
    CircleImageView civ_author;
    Map<String, String> parsed;
    ExpandableListView elv_main;
    DisplayMetrics metrics;

    EditText et_comment;
    ImageButton bt_send;

    String url;

    ArrayList<ArrayList<Element>> groups = new ArrayList<ArrayList<Element>>();
    ArrayList<Element> info = new ArrayList<Element>();
    ArrayList<Element> commentse = new ArrayList<Element>();
    ArrayList<Element> attachments = new ArrayList<Element>();
   // ListView lv_com;

    boolean isAuthor = false;
    private String bug_report_hash;
    BugExpandableAdapter bea;

    AsyncTask<String, Void, Document> get_hash;

    ProgressBar pbload;
    RelativeLayout mainc;

    private final Pattern REGEX_DELETE_COMMENT = Pattern.compile("BugTracker.removeComment\\('(.*)','(.*)'\\)");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bug);
        setDragEdge(SwipeBackLayout.DragEdge.LEFT);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);

        //https://vk.com/bugtracker?act=show&id=49971

        if(savedInstanceState != null){
            url = savedInstanceState.getString("url");
            bug_report_hash = savedInstanceState.getString("bt_hash");
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        author = findViewById(R.id.txt_author);
        time = findViewById(R.id.txt_time);
        name =  findViewById(R.id.txt_name);
        comment = findViewById(R.id.txt_comment);
        Linkify.addLinks(comment, Linkify.WEB_URLS);

        bt_send = findViewById(R.id.button_chatbox_send);
        bt_send.setImageDrawable(getResources().getDrawable(R.drawable.ic_send_vk_24dp));
        et_comment = findViewById(R.id.edittext_chatbox);

        civ_author = findViewById(R.id.img_author);
        elv_main = (ExpandableListView)findViewById(R.id.lv_new);

        pbload = findViewById(R.id.pb_loading);
        mainc = findViewById(R.id.mainc);

        parsed = Authdata.getCookies(getApplicationContext());
        url = getIntent().getStringExtra("url");

        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;

        bt_send.setOnClickListener(this);
        elv_main.setIndicatorBounds(width - GetDipsFromPixel(50), width - GetDipsFromPixel(10));

        registerForContextMenu(elv_main);

        pbload.setIndeterminate(true);
        pbload.setVisibility(View.VISIBLE);
        mainc.setVisibility(View.INVISIBLE);

        Callback cb1 = new mCallGet();
        Callback cb2 = new mCallHash();

        new CookedDocument(this, cb1, "https://vk.com" + url).execute();
        get_hash = new CookedDocument(this, cb2, "https://vk.com" + url + "&al=1").execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.simple_black_browser, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_delete).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_browser:
                Intent i_browser = new Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse("https://vk.com" + url));
                startActivity(i_browser);
                break;
            case R.id.action_copy:
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "https://vk.com" + url);
                startActivity(Intent.createChooser(sharingIntent, "Ссылка на отчёт"));
                break;
          /*  case R.id.action_delete:
                String id = Uri.parse(url).getQueryParameter("id");
                new CookedDocument(getApplicationContext(), new Callback() {
                    @Override
                    public void onError(Exception e) {
                        if(e instanceof AccessDeniedBtException) {
                            Toast.makeText(getApplicationContext(), R.string.error_access_denied, Toast.LENGTH_SHORT).show();
                        }
                        else if(e instanceof NoInternetException) {
                            Toast.makeText(getApplicationContext(), R.string.error_no_internet, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onSuccess(Document d) {
                        Toast.makeText(ActivityBug.this, "Отчёт успешно удалён", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }, "https://vk.com/bugtracker?act=a_remove_bugreport&id="+id+"&hash="+bug_report_hash).execute("");
                System.out.println("That's Work");
                break; */
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        Log.i("", "Click");
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);

        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            switch (groupPosition) {
                case 2:
                    if (commentse.get((int)info.packedPosition).select("div.bt_report_cmt_author a.bt_report_cmt_author_a").attr("href").isEmpty()) {break;}
                    Uri userComment = Uri.parse("https://vk.com"+commentse.get((int)info.packedPosition).select("div.bt_report_cmt_author a.bt_report_cmt_author_a").attr("href"));
                    menu.setHeaderTitle(R.string.see);
                    menu.add(0, 0, 0, R.string.profile_see);

                    if(userComment.getQueryParameter("id").equals(Authdata.getUserId(getApplicationContext()))) {
                        menu.add(0, 1, 0, "Удалить комментарий");
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        switch (menuItemIndex) {
            case 0:
                if(commentse.get((int)info.packedPosition).select("div.bt_report_cmt_author a.bt_report_cmt_author_a").attr("href").isEmpty()){break;}
                String url_user = "https://vk.com/"+commentse.get((int)info.packedPosition).select("div.bt_report_cmt_author a.bt_report_cmt_author_a").attr("href");
                Intent profile_user_item = new Intent(this, ActivityFragmentWrapper.class);
                profile_user_item.putExtra("set", 1);
                profile_user_item.putExtra("url", url_user);
                startActivity(profile_user_item);
                break;
            case 1:
                if(commentse.get((int)info.packedPosition).select("div.bt_report_cmt_author a.bt_report_cmt_author_a").attr("href").isEmpty()){break;}
                String rComment = commentse.get((int)info.packedPosition).select("div.bt_report_cmt_content div.post_actions div.reply_delete_button").attr("onclick");
                Matcher m = REGEX_DELETE_COMMENT.matcher(rComment);
                while(m.find()) {
                    String id = m.group(1);
                    String commentHash = m.group(2);

                    new CookedDocument(getApplicationContext(), new Callback() {
                        @Override
                        public void onError(Exception e) {
                            if(e instanceof AccessDeniedBtException) {
                                Toast.makeText(getApplicationContext(), R.string.error_access_denied, Toast.LENGTH_SHORT).show();
                            }
                            else if(e instanceof NoInternetException) {
                                Toast.makeText(getApplicationContext(), R.string.error_no_internet, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onSuccess(Document d) {
                            commentse.remove((int)info.packedPosition);

                            bea.notifyDataSetChanged();
                        }
                    }, "https://vk.com/bugtracker?act=remove_comment&id="+id+"&hash="+commentHash).execute("");
                }

                //"div.bt_report_cmt_content div.post_actions div.reply_delete_button"
                break;
        }

        return super.onContextItemSelected(item);
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
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.button_chatbox_send:
                if (et_comment.getText().toString().isEmpty()) {
                    Toast.makeText(this, R.string.empty_comment, Toast.LENGTH_SHORT).show();
                } else {
                    new Post_New_Comment(this).execute(et_comment.getText().toString());
                }
                break;
        }
    }


    class mCallGet extends Callback {
        @Override
        public void onError(Exception e) {
            Looper.prepare();
            if(e instanceof AccessDeniedBtException) {
                Toast.makeText(getApplicationContext(), R.string.error_access_denied, Toast.LENGTH_SHORT).show();
                get_hash.cancel(true);
            }
            else if(e instanceof NoInternetException) {
                Toast.makeText(getApplicationContext(), R.string.error_no_internet, Toast.LENGTH_SHORT).show();
                get_hash.cancel(true);
            }
        }

        @Override
        public void onSuccess(Document s) {
            pbload.setVisibility(View.INVISIBLE);
            mainc.setVisibility(View.VISIBLE);
            Elements author_time = s.select("div.bt_report_one_author_content");
            if(author_time.get(0).select("a").attr("href").contains(Authdata.getUserId(getApplicationContext()))) {
                isAuthor = true;
            }
            author.setText(author_time.select("a").text());
            time.setText(author_time.select("div.bt_report_one_author_date").text());

            name.setText(s.select("div.bt_report_one_title").text());
            comment.setText(Html.fromHtml(s.select("div.bt_report_one_descr").html()));


            for (Element i3: s.select(".bt_report_one_attachs a")) {
                if(i3.attr("class").startsWith("page_doc_icon")){
                    continue;
                }
                attachments.add(i3);
            }
            groups.add(attachments);

            for (Element i2: s.select("div.bt_report_one_info_row.clear_fix")) {
                info.add(i2);
            }
            groups.add(info);

            for(Element i : s.select("div.bt_report_cmt_wrap.clear_fix")) {
                commentse.add(i);
            }
            groups.add(commentse);


            bea = new BugExpandableAdapter(getApplicationContext(), groups);
            elv_main.setAdapter(bea);
            elv_main.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                    switch(i) {
                        case 0:
                            Element attach = attachments.get(i1);
                            if (attach.attr("class").contains("page_doc_title")) {

                                Intent i_doc = new Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse("https://vk.com"+attach.attr("href")));
                                startActivity(i_doc);
                                break;
                            }

                            Pattern p = Pattern.compile("\\{(.*)\\}");
                            Matcher m = p.matcher(attach.attr("onclick"));

                            while (m.find()) {
                                JSONObject reader = null;
                                try {
                                    reader = new JSONObject(m.group().replace("queue", "\"queue\""));
                                    JSONObject response = reader.getJSONObject("temp");


                                    String last_element = Utils.getLastElement(response.keys());
                                    String base = response.getString("base");
                                    JSONArray intemp = response.optJSONArray(last_element);
                                    String url = (String) intemp.get(0);
                                    if (url.startsWith("https://")) {
                                        Intent i_image = new Intent(getApplicationContext(), ImageViewer.class);
                                        i_image.putExtra("url", url + ".jpg");
                                        startActivity(i_image);
                                    } else {
                                        Intent i_image = new Intent(getApplicationContext(), ImageViewer.class);
                                        i_image.putExtra("url", base + url + ".jpg");
                                        startActivity(i_image);
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                    }

                    return false;
                }
            });

            final String img_src = s.select("img.bt_report_one_author__img").attr("src");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Picasso.with(getApplicationContext()).load(img_src).into(civ_author);
                }
            });
        }
    }

    class mCallHash extends Callback {
        @Override
        public void onSuccess(Document d) {
            Pattern p = Pattern.compile("cur.bugreportHash = '(.*?)'");
            Matcher m = p.matcher(d.html());

            while (m.find()) {
                bug_report_hash = m.group(1);
            }
        }
    }

    class Post_New_Comment extends AsyncTask<String, Void, Document[]> {

        Document[] doc = new Document[2];
        private ProgressDialog dialog;

        public Post_New_Comment(AppCompatActivity activity) {
            dialog = new ProgressDialog(activity);
    }

        @Override
        protected void onPreExecute() {
            dialog.setIndeterminate(true);
            dialog.setMessage(getResources().getString(R.string.wait_plz));
            dialog.show();
        }

        @Override
        protected Document[] doInBackground(String... strings) {
            try {

                Uri myurl = Uri.parse("https://vk.com"+url);
                doc[0] = Jsoup.connect("https://vk.com/bugtracker?act=a_send_comment&report_id="+myurl.getQueryParameter("id")
                        +"&hash="+bug_report_hash+"&message="
                        +strings[0]).cookies(parsed).get();
                doc[1] = Jsoup.connect("https://vk.com"+url).cookies(parsed).get();
            } catch (IOException e){
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                Toast.makeText(ActivityBug.this, R.string.error_base_1, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document[] document) {
            super.onPostExecute(document);
            if (dialog.isShowing()) {
                dialog.dismiss();
                et_comment.setText("");
            }

            Toast.makeText(ActivityBug.this, R.string.comment_added, Toast.LENGTH_SHORT).show();

            commentse.clear();
            for(Element i : document[1].select("div.bt_report_cmt_wrap.clear_fix")) {
                commentse.add(i);
            }

            bea.notifyDataSetChanged();
        }
    }

    public int GetDipsFromPixel(float pixels)
    {
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (pixels * scale + 0.5f);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("url", url);
        outState.putString("bt_has", bug_report_hash);
    }

}
