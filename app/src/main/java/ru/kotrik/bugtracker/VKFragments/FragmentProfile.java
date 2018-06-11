package ru.kotrik.bugtracker.VKFragments;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import ru.kotrik.bugtracker.VKActivities.ActivityFragmentWrapper;
import ru.kotrik.bugtracker.Adapters.ProductAdapter;
import ru.kotrik.bugtracker.Authdata;
import ru.kotrik.bugtracker.Helpers.Async.CookedDocument;
import ru.kotrik.bugtracker.Helpers.Callback;
import ru.kotrik.bugtracker.Helpers.Exceptions.AccessDeniedBtException;
import ru.kotrik.bugtracker.Helpers.Exceptions.NoInternetException;
import ru.kotrik.bugtracker.Models.ReportProductsProfile;
import ru.kotrik.bugtracker.R;

/**
 * Created by kotoriku on 30.03.2018.
 */

public class FragmentProfile extends Fragment {

    TextView name, status;
    CircleImageView avatar;
    ListView products;
    ArrayList<ReportProductsProfile> reports;

    String[] url = new String[2];
    AsyncTask<String, Void, Document> U_Profile, U_Profile_products;

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.profile_fragment, container, false);
        final Toolbar mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        setHasOptionsMenu(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setIcon(R.drawable.ic_vk);

        url[0] = getActivity().getIntent().getStringExtra("url");
        if(savedInstanceState != null){
            url[0] = savedInstanceState.getString("url");
            url[1] = savedInstanceState.getString("url_full");
        }
        if (url[0] == null) {
            url[0] = "https://vk.com/bugtracker?act=reporter&id=" + Authdata.getUserId(getContext());
        }
        Uri iduser = Uri.parse(url[0]);
        if (url[1] == null) {
            url[1] = "https://vk.com/bugtracker?act=reporter_products&id=" + iduser.getQueryParameter("id");
        }

        setRetainInstance(true);

        name = view.findViewById(R.id.txt_name);
        status = view.findViewById(R.id.txt_status);
        avatar = view.findViewById(R.id.img_avatar);

        products = view.findViewById(R.id.lv_items);

        Callback cb1 = new mCallbackInfo();
        Callback cb2 = new mCallbackProducts();
        U_Profile = new CookedDocument(getContext(), cb1, url[0]).execute();
        U_Profile_products = new CookedDocument(getContext(), cb2, url[1]).execute();

        registerForContextMenu(products);
        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        Log.i("", "Click");
        AdapterView.AdapterContextMenuInfo mi =(AdapterView.AdapterContextMenuInfo) menuInfo;
        if (reports.get(mi.position).url_report.isEmpty()){
            return;
        }
        menu.setHeaderTitle(R.string.details);
        menu.add(0, 0, 0, R.string.reports_by_user);
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        switch (menuItemIndex) {
            case 0:
                if(reports.get(info.position).url_report.isEmpty()){break;}
                String url_user = "https://vk.com"+reports.get(info.position).url_report;
                Intent profile_tracker_item = new Intent(getContext(), ActivityFragmentWrapper.class);
                profile_tracker_item.putExtra("set", 2);
                profile_tracker_item.putExtra("url", url_user);
                startActivity(profile_tracker_item);
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.simple_browser, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_browser:
                Intent i_browser = new Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse(url[0]));
                startActivity(i_browser);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    class mCallbackInfo extends Callback {
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
            String avatar_url = s.select(".bt_reporter_icon img.bt_reporter_icon_img").attr("src");
            String vk_name = s.select("div.bt_reporter_name a.mem_link").text();
            String vk_url = s.select("div.bt_reporter_name a.mem_link").attr("href");
            if(vk_url.isEmpty()){
                Toast.makeText(getContext(), R.string.profile_not_found, Toast.LENGTH_SHORT).show();
                getActivity().finish();
                U_Profile_products.cancel(true);
                return;
            }
            String vk_status = s.select("div.bt_reporter_content_block").text();

            name.setText(vk_name);
            status.setText(vk_status);
            Picasso.with(getActivity()).load(avatar_url).into(avatar);
        }
    }

    class mCallbackProducts extends Callback {
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
        public void onSuccess(Document d) {
            reports = new ArrayList<ReportProductsProfile>();
            for(Element i1: d.select("div.bt_reporter_product")) {
                reports.add(new ReportProductsProfile(
                        i1.select("img.bt_reporter_product_img").attr("src"),
                        i1.select("div.bt_reporter_product_title").text(),
                        i1.select("div.bt_reporter_product_title a").attr("href"),
                        i1.select("a.bt_reporter_product_nreports").text(),
                        i1.select("a.bt_reporter_product_nreports").attr("href")
                ));
            }
            ProductAdapter pa = new ProductAdapter(getContext(), R.layout.bug_product, reports);
            products.setAdapter(pa);
            products.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    getActivity().openContextMenu(view);
                }
            });
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("url", url[0]);
        outState.putString("url_full", url[1]);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        U_Profile.cancel(true);
    }
}
