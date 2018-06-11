package ru.kotrik.bugtracker.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.jsoup.nodes.Document;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import ru.kotrik.bugtracker.VKActivities.ActivityFragmentWrapper;
import ru.kotrik.bugtracker.Authdata;
import ru.kotrik.bugtracker.Helpers.Async.CookedDocument;
import ru.kotrik.bugtracker.Helpers.Callback;
import ru.kotrik.bugtracker.Helpers.Exceptions.AccessDeniedBtException;
import ru.kotrik.bugtracker.Helpers.Exceptions.NoInternetException;
import ru.kotrik.bugtracker.Helpers.Utils;
import ru.kotrik.bugtracker.R;
import ru.kotrik.bugtracker.VKActivities.ActivityAboutApp;

public class CardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        System.out.println(viewType);
        if(viewType == 0) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_card_profile_info, parent, false);
            return new Profile(v);
        }
        else if(viewType == 1) {
            RecyclerView lv = new RecyclerView(parent.getContext());
            lv.setLayoutManager(new LinearLayoutManager(parent.getContext()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                lv.setElevation(3);
            }
            lv.setBackgroundColor(Color.WHITE);
            lv.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));

            return new Items(lv);
        }
        else if(viewType == 2){
            LinearLayout linearLayout = new LinearLayout(parent.getContext());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setBackgroundColor(Color.WHITE);
            linearLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                linearLayout.setElevation(3);
            }

            TextView btn = new TextView(parent.getContext());
            btn.setText(R.string.logout);
            btn.setPadding(
                    Utils.dpAsPixel(parent.getContext(), 16),
                    Utils.dpAsPixel(parent.getContext(), 16),
                    0,
                    Utils.dpAsPixel(parent.getContext(), 16)
            );
            btn.setTextAppearance(parent.getContext(), R.style.TextAppearance_AppCompat_Body2);
            btn.setTextColor(Color.RED);

            linearLayout.addView(btn);
            return new LeaveButton(linearLayout);
        }
        throw new RuntimeException("No match for " + viewType + ".");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class Profile extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView name, count;
        public CircleImageView civ;
        private Activity activity;

        public Profile(View itemView) {
            super(itemView);
            activity = (Activity) itemView.getContext();
            itemView.setOnClickListener(this);

            name = itemView.findViewById(R.id.txt_name);
            count = itemView.findViewById(R.id.txt_status);

            civ = itemView.findViewById(R.id.img_avatar);

            new CookedDocument(itemView.getContext(),
                               new cb1(),
                               "https://vk.com/bugtracker?act=reporter&id=" + Authdata.getUserId(itemView.getContext())
            ).execute();
        }

        @Override
        public void onClick(View view) {
            Intent i = new Intent(view.getContext(), ActivityFragmentWrapper.class);
            i.putExtra("url", "https://vk.com/bugtracker?act=reporter&id=" + Authdata.getUserId(view.getContext()));
            i.putExtra("set", 1);
            view.getContext().startActivity(i);
        }

        class cb1 extends Callback {
            @Override
            public void onError(Exception e) {
                if(e instanceof AccessDeniedBtException) {
                    Toast.makeText(itemView.getContext(), R.string.error_access_denied, Toast.LENGTH_SHORT).show();
                    activity.finish();
                }
                else if(e instanceof NoInternetException) {
                    Toast.makeText(itemView.getContext(), R.string.error_no_internet, Toast.LENGTH_SHORT).show();
                    activity.finish();
                }
            }

            @Override
            public void onSuccess(Document s) {
                String avatar_url = s.select(".bt_reporter_icon img.bt_reporter_icon_img").attr("src");
                String vk_name = s.select("div.bt_reporter_name a.mem_link").text();
                String vk_url = s.select("div.bt_reporter_name a.mem_link").attr("href");
                if(vk_url.isEmpty()){
                    Toast.makeText(itemView.getContext(), R.string.profile_not_found, Toast.LENGTH_SHORT).show();
                    activity.finish();
                    return;
                }
                String vk_status = s.select("div.bt_reporter_content_block").text();

                name.setText(vk_name);
                count.setText(vk_status);
                Picasso.with(itemView.getContext()).load(avatar_url).into(civ);
            }
        }
    }

    class Items extends RecyclerView.ViewHolder {

        public RecyclerView rv;

        public Items(View itemView) {
            super(itemView);
            rv = ((RecyclerView) itemView);
            ArrayList<Item> items = new ArrayList<Item>();
            items.add(new Item(R.drawable.ic_bug_report_gray_24dp, R.string.menu_my_reports));
            items.add(new Item(R.drawable.ic_bookmark_gray_24dp, R.string.menu_bookmark));
            items.add(new Item(R.drawable.ic_nighttheme_gray_24dp, R.string.menu_night_theme));
            items.add(new Item(R.drawable.ic_info_gray_24dp, R.string.menu_about_app));
            rv.setAdapter(new ItemAdapter(items));
        }

        class Item {
            public int icon;
            public int name;

            public Item(int icon, int name) {
                this.icon = icon;
                this.name = name;
            }
        }

        class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

            public ArrayList<Item> items;

            public ItemAdapter(ArrayList<Item> items) {
                this.items = items;
            }

            @Override
            public ItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_card_list_item, parent, false);
                return new ViewHolder(v);
            }

            @Override
            public void onBindViewHolder(ItemAdapter.ViewHolder holder, int position) {
                Item i = items.get(position);
                holder.civ.setImageDrawable(itemView.getResources().getDrawable(i.icon));
                holder.name.setText(i.name);
            }

            @Override
            public int getItemCount() {
                return items.size();
            }

            class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

                public TextView name;
                public CircleImageView civ;

                public ViewHolder(View itemView) {
                    super(itemView);
                    name = itemView.findViewById(R.id.txt_name);
                    civ = itemView.findViewById(R.id.image);
                    itemView.setOnClickListener(this);
                }

                @Override
                public void onClick(View view) {
                    switch(getAdapterPosition()) {
                        case 0:
                            Intent i = new Intent(view.getContext() , ActivityFragmentWrapper.class);
                            i.putExtra("url", "https://vk.com/bugtracker?mid="+Authdata.getUserId(view.getContext())+"&status=100");
                            i.putExtra("set", 2);
                            view.getContext().startActivity(i);
                            break;
                        case 3:
                            Intent i2 = new Intent(view.getContext(), ActivityAboutApp.class);
                            view.getContext().startActivity(i2);
                            break;
                        default:
                            Toast.makeText(itemView.getContext(), "В разработке!", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }

        }

    }

    class LeaveButton extends RecyclerView.ViewHolder implements View.OnClickListener {

        public LeaveButton(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Authdata.removeAuth(itemView.getContext());
            Activity activity = (Activity) itemView.getContext();
            activity.finish();
        }
    }
}
