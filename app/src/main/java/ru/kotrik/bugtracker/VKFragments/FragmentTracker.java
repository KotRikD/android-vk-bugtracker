package ru.kotrik.bugtracker.VKFragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;


import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import ru.kotrik.bugtracker.Adapters.BugAdapter;
import ru.kotrik.bugtracker.Helpers.Async.CookedDocument;
import ru.kotrik.bugtracker.Helpers.Callback;
import ru.kotrik.bugtracker.Helpers.Exceptions.AccessDeniedBtException;
import ru.kotrik.bugtracker.Helpers.Exceptions.NoInternetException;
import ru.kotrik.bugtracker.Models.Bug;
import ru.kotrik.bugtracker.R;
import ru.kotrik.bugtracker.VKActivities.ActivityAddNewReport;

/**
 * Created by kotoriku on 30.03.2018.
 */

public class FragmentTracker extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    RecyclerView lvbugs;
    ArrayList<Bug> bugs;
    String url;
    ProgressBar pbload;
    FloatingActionButton fab;

    BugAdapter ba;
    AsyncTask<String, Void, Document> G_Reports;

    View view;
    Callback cb1;
    SwipeRefreshLayout srf;

    long min_udate;
    long max_udate;
    boolean isLoading;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.tracker_fragment, container, false);
        final Toolbar mToolbar = (Toolbar) view.findViewById(R.id.toolbar_t);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        setHasOptionsMenu(true);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setIcon(R.drawable.ic_vk);
        mToolbar.inflateMenu(R.menu.search);

        setRetainInstance(true);

        pbload = view.findViewById(R.id.pb_loading);
        fab = view.findViewById(R.id.fab);

        srf = view.findViewById(R.id.srf);

        lvbugs = view.findViewById(R.id.list_bugs);
        LinearLayoutManager llmlvbugs = new LinearLayoutManager(getContext());
        lvbugs.setLayoutManager(llmlvbugs);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(lvbugs.getContext(),
                llmlvbugs.getOrientation());
        lvbugs.addItemDecoration(dividerItemDecoration);

        long unixTime = System.currentTimeMillis() / 1000L;
        min_udate = unixTime-86400;
        max_udate = unixTime-86400;


        final Callback cb2 = new Callback2();
        if (getActivity().getIntent().getStringExtra("url") == null) {
            url = "https://vk.com/al_bugtracker.php?min_udate=" + String.valueOf(min_udate);
            lvbugs.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int totalItemCount = lvbugs.getLayoutManager().getItemCount();
                    int lastVisibleItem = ((LinearLayoutManager)lvbugs.getLayoutManager()).findLastVisibleItemPosition();
                    int visibleThreshold = 5;
                    if(!isLoading && totalItemCount <= lastVisibleItem + visibleThreshold) {
                        isLoading = true;
                        min_udate = min_udate - 86400;

                        String mUrl = "https://vk.com/al_bugtracker.php?min_udate=" + String.valueOf(min_udate) + "&max_udate=" + String.valueOf(max_udate);

                        new CookedDocument(getContext(), cb2, mUrl).execute();
                    }

                    if (dy > 0 && fab.getVisibility() == View.VISIBLE) {
                        fab.hide();
                    } else if (dy < 0 && fab.getVisibility() != View.VISIBLE) {
                       fab.show();
                    }

                }
            });
        } else {
            url = getActivity().getIntent().getStringExtra("url");
        }

        srf.setOnRefreshListener(this);
        srf.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));

        pbload.setIndeterminate(true);
        lvbugs.setHasFixedSize(true);


        cb1 = new Callback1();
        G_Reports  = new CookedDocument(getContext(), cb1, url).execute();

        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(view.getContext(), ActivityAddNewReport.class);
                view.getContext().startActivity(i);
            }
        });

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                System.out.println("hello");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        G_Reports.cancel(true);
    }

    @Override
    public void onRefresh() {
        pbload.setVisibility(View.VISIBLE);
        long unixTime = System.currentTimeMillis() / 1000L;
        min_udate = unixTime-86400;
        max_udate = unixTime-86400;

        bugs.clear();
        ba.notifyDataSetChanged();
        G_Reports = new CookedDocument(getContext(), cb1, url).execute();
        srf.setRefreshing(false);
        pbload.setVisibility(View.INVISIBLE);
    }


    class Callback1 extends Callback{
        @Override
        public void onBackground() {
            try {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pbload.setVisibility(View.VISIBLE);
                        lvbugs.setVisibility(View.GONE);
                        fab.setVisibility(View.GONE);
                    }
                });
            } catch(NullPointerException e) {
                return;
            }
        }

        @Override
        public void onError(Exception e) {
            Looper.prepare();
            if(e instanceof AccessDeniedBtException) {
                Toast.makeText(getActivity(), R.string.error_no_internet, Toast.LENGTH_SHORT).show();
            }
            else if(e instanceof NoInternetException) {
                Toast.makeText(getActivity(), R.string.error_no_internet, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onSuccess(Document s) {
            final Elements Name_screen = s.select("h3.bt_reports_found");
            if(bugs == null){
                bugs = new ArrayList<Bug>();
            }
            bugs.add(new Bug(null, null, null, new ArrayList<String>(), null));
            Elements elList = s.select("div.bt_report_row_wrap");
            for(Element el: elList) {
                String text = el.select("a.bt_report_title_link").text();
                String link = el.select("a.bt_report_title_link").attr("href");
                String time = el.select("div.bt_report_info_details").text();
                String type = el.select("span.bt_report_info__value").text();

                ArrayList<String> tags = new ArrayList<String>();
                Elements elTags = el.select("div.bt_tag_label");
                for (Element el2 : elTags) {
                    tags.add(el2.text());
                }
                bugs.add(new Bug(text, time, type, tags, link));
            }

            if (lvbugs.getAdapter() == null) {
                ba = new BugAdapter(bugs, Name_screen.text());
                lvbugs.setAdapter(ba);
            } else {
                ba.notifyDataSetChanged();
            }


            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pbload.setVisibility(View.GONE);
                    lvbugs.setVisibility(View.VISIBLE);
                    fab.setVisibility(View.VISIBLE);
                }
            });

        }
    }

    class Callback2 extends Callback {
        @Override
        public void onError(Exception e) {
            Looper.prepare();
            if(e instanceof AccessDeniedBtException) {
                Toast.makeText(getActivity(), R.string.error_access_denied, Toast.LENGTH_SHORT).show();
            }
            else if(e instanceof NoInternetException) {
                Toast.makeText(getActivity(), R.string.error_no_internet, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onSuccess(Document s) {
            Elements elList = s.select("div.bt_report_row_wrap");
            for(Element el: elList) {
                String text = el.select("a.bt_report_title_link").text();
                String link = el.select("a.bt_report_title_link").attr("href");
                String time = el.select("div.bt_report_info_details").text();
                String type = el.select("span.bt_report_info__value").text();

                ArrayList<String> tags = new ArrayList<String>();
                Elements elTags = el.select("div.bt_tag_label");
                for (Element el2 : elTags) {
                    tags.add(el2.text());
                }

                bugs.add(new Bug(text, time, type, tags, link));
            }

            max_udate = min_udate;
            isLoading = false;
            ba.notifyDataSetChanged();
        }
    }
}
