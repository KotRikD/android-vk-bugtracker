package ru.kotrik.bugtracker.VKFragments;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.kotrik.bugtracker.VKActivities.ActivityFragmentWrapper;
import ru.kotrik.bugtracker.Adapters.ProductAllItemAdapter;
import ru.kotrik.bugtracker.Adapters.ProductItemAdapter;
import ru.kotrik.bugtracker.Authdata;
import ru.kotrik.bugtracker.Helpers.Async.CookedDocument;
import ru.kotrik.bugtracker.Helpers.Callback;
import ru.kotrik.bugtracker.Helpers.Exceptions.AccessDeniedBtException;
import ru.kotrik.bugtracker.Helpers.Exceptions.NoInternetException;
import ru.kotrik.bugtracker.Models.ProductItem;
import ru.kotrik.bugtracker.Models.ProductItemAll;
import ru.kotrik.bugtracker.R;

public class FragmentProducts extends Fragment {

    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;

    static Map<String, String> parsed;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.products_tabs, container, false);

        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.ic_vk);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.products);

        setRetainInstance(true);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(1);


        parsed = Authdata.getCookies(getContext());
        return view;
    }

    public void setupViewPager(ViewPager vp) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        FragmentProductsAll all = new FragmentProductsAll();
        FragmentProductsMy my = new FragmentProductsMy();
        adapter.addFragment(all, getResources().getString(R.string.all_products));

        adapter.addFragment(my, getResources().getString(R.string.my_products));
        vp.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @SuppressLint("ValidFragment")
    public static class FragmentProductsAll extends Fragment {

        View view;
        ListView list;
        ArrayList<ProductItemAll> items;

        AsyncTask<String, Void, Document> getProducts;

        private Pattern acceptLicense = Pattern.compile("BugTracker\\.joinProduct\\((.*)\\);");
        private Pattern decileLicense = Pattern.compile("BugTracker\\.deleteLicenceRequest\\((.*)\\);");

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            view = inflater.inflate(R.layout.products_tabs_all, container, false);

            list = view.findViewById(R.id.lv_items);

            getProducts = new CookedDocument(getContext(), new cb1(), "https://vk.com/bugtracker?act=products&section=all").execute();
            return view;
        }


        class cb1 extends Callback {
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
                Elements productse = d.select("div.bt_product_row.clear_fix");
                if(items == null) {
                    items = new ArrayList<ProductItemAll>();
                } else {
                    items.clear();
                }
                for (Element i : productse) {
                    String name = i.select("a.bt_prod_link").text();
                    String url = i.select("a.bt_prod_link").attr("href");

                    String img_url = i.select("img.bt_prod_one_photo__img").attr("src");

                    Uri urlproduct = Uri.parse("https://vk.com"+url);
                    String id = urlproduct.getQueryParameter("id");
                    boolean isRequest = false;
                    String btHash = null;

                    try{
                    if(i.select("div.bt_product_row_join > button.flat_button").isEmpty()){
                        Matcher m = decileLicense.matcher(i.select("div.bt_product_row_join__note > a").attr("onclick"));

                        while(m.find()){
                            String[] splitted = m.group(1).replace(" ", "").split(",");
                            try {
                                String hash = splitted[1].replace("'", "");
                                isRequest = false;
                                btHash = new String(hash);
                            } catch(Exception e){
                                e.printStackTrace();
                                return;
                            }
                        }
                    } else if (i.select("div.bt_product_row_join__note > a").isEmpty()){
                        Matcher m = acceptLicense.matcher(i.select("div.bt_product_row_join > button.flat_button").attr("onclick"));

                        while(m.find()){
                            String[] splitted = m.group(1).replace(" ", "").split(",");
                            try {
                                String hash = splitted[3].replace("'", "");
                                isRequest = true;
                                btHash = new String(hash);
                            } catch(Exception e){
                                e.printStackTrace();
                                return;
                            }
                        }
                    }
                    } catch (Exception e) {
                        e.printStackTrace();
                        getActivity().finish();
                    }
                    items.add(new ProductItemAll(id, img_url, name, isRequest, btHash));

                }
                if(list.getAdapter() == null) {
                    ProductAllItemAdapter pia = new ProductAllItemAdapter(view.getContext(), R.layout.products_tabs_all_item, items, this);
                    list.setAdapter(pia);
                } else {
                    ((ProductAllItemAdapter) list.getAdapter()).notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            getProducts.cancel(true);
        }
    }

    @SuppressLint("ValidFragment")
    public static class FragmentProductsMy extends Fragment {

        AsyncTask<String, Void, Document> get_products;
        ListView products;
        ArrayList<ProductItem> items;
        View view;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            view = inflater.inflate(R.layout.products_tabs_my, container, false);

            products = view.findViewById(R.id.lv_products);

            Callback mCall = new cb();
            get_products = new CookedDocument(getContext(), mCall, "https://vk.com/bugtracker?act=products").execute();
            registerForContextMenu(products);

            products.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View viewd, int i, long l) {
                   getActivity().openContextMenu(viewd);
                }
            });
            return view;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            super.onCreateContextMenu(menu, v, menuInfo);
            Log.i("", "Click");
            AdapterView.AdapterContextMenuInfo mi =(AdapterView.AdapterContextMenuInfo) menuInfo;

            menu.setHeaderTitle("Подробности");
            menu.add(0, 10, 0, R.string.reports_on_product);
            menu.add(0, 11, 0, R.string.about_product);
        }


        @Override
        public boolean onContextItemSelected(MenuItem item) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            int menuItemIndex = item.getItemId();
            switch (menuItemIndex) {
                case 10:
                    Uri data = Uri.parse("https://vk.com"+items.get(info.position).product_url);
                    String urldd = "https://vk.com/al_bugtracker.php?min_udate=1&product="+data.getQueryParameter("id");
                    Intent profile_tracker_itemsd = new Intent(getContext(), ActivityFragmentWrapper.class);
                    profile_tracker_itemsd.putExtra("set", 2);
                    profile_tracker_itemsd.putExtra("url", urldd);
                    startActivity(profile_tracker_itemsd);
                    break;
                case 11:
                    Uri data2 = Uri.parse("https://vk.com"+items.get(info.position).product_url);
                    Intent i_doc = new Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse("https://vk.com/bugtracker?act=product&id="+data2.getQueryParameter("id")));
                    startActivity(i_doc);
                    break;
            }

            return super.onContextItemSelected(item);
        }

        class cb extends Callback {
            @Override
            public void onSuccess(Document d) {
                Elements productse = d.select("div.bt_product_row.clear_fix");
                items = new ArrayList<ProductItem>();
                for (Element i : productse) {
                    String name = i.select("a.bt_prod_link").text();
                    String url = i.select("a.bt_prod_link").attr("href");

                    Elements subtitles = i.select("div.bt_product_row_subtitle");
                    String count = subtitles.get(0).text();
                    String update = "";
                    if(subtitles.size() > 1) { update = subtitles.get(1).text(); }

                    String img_url = i.select("img.bt_prod_one_photo__img").attr("src");

                    items.add(new ProductItem(img_url, name, count, update, url));
                }
                ProductItemAdapter pia = new ProductItemAdapter(getContext(), R.layout.products_tabs_my_item, items);
                products.setAdapter(pia);
            }

            @Override
            public void onError(Exception e) {
                Looper.prepare();
                if(e instanceof AccessDeniedBtException) {
                    Toast.makeText(getActivity(), R.string.error_access_denied, Toast.LENGTH_SHORT).show();
                }
                else if(e instanceof NoInternetException) {
                    Toast.makeText(getActivity(), R.string.error_access_denied, Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            get_products.cancel(true);
        }
    }


}
