package ru.kotrik.bugtracker.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import ru.kotrik.bugtracker.R;
import ru.kotrik.bugtracker.Helpers.Utils;

/**
 * Created by kotoriku on 24.03.2018.
 */

public class BugExpandableAdapter extends BaseExpandableListAdapter {

    private ArrayList<ArrayList<Element>> mGroups;
    private Context mContext;

    public BugExpandableAdapter (Context context,ArrayList<ArrayList<Element>> groups){
        mContext = context;
        mGroups = groups;
    }

    @Override
    public int getGroupCount() {
        return mGroups.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return mGroups.get(i).size();
    }

    @Override
    public Object getGroup(int i) {
        return mGroups.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return mGroups.get(i).get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.bug_group, null);
        }

        TextView nameGroup = view.findViewById(R.id.txt_group);
        TextView count = view.findViewById(R.id.txt_count);
        ImageView imgGroup = view.findViewById(R.id.img_group);

        switch (i) {
            case 0:
                nameGroup.setText(R.string.attachments);
                imgGroup.setImageDrawable(view.getResources().getDrawable(R.drawable.ic_attachment_black_24dp));
                count.setText(String.valueOf(mGroups.get(i).size()));
                break;
            case 1:
                nameGroup.setText(R.string.reduced_info);
                imgGroup.setImageDrawable(view.getResources().getDrawable(R.drawable.ic_info_black_24dp));
                break;
            case 2:
                nameGroup.setText(R.string.info_comments);
                imgGroup.setImageDrawable(view.getResources().getDrawable(R.drawable.ic_comment_black_24dp));
                count.setText(String.valueOf(mGroups.get(i).size()));
                break;
        }
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        if (view == null || (Integer)view.getTag() != i) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            switch (i) {
                case 0:
                    view = inflater.inflate(R.layout.bug_attach, null);
                    view.setTag(0);
                    break;
                case 1:
                    view = inflater.inflate(R.layout.bug_info, null);
                    view.setTag(1);
                    break;
                case 2:
                    view = inflater.inflate(R.layout.bug_comment, null);
                    view.setTag(2);
                    break;
                default:
                    break;
            }
        }

        switch(i) {
            case 0:
                Element attachs = mGroups.get(i).get(i1);
                TextView txt_name = view.findViewById(R.id.txt_name);
                CircleImageView img = view.findViewById(R.id.img_add);
                if (attachs.attr("class").contains("page_doc_title")) {
                    txt_name.setText(attachs.html());
                    img.setImageDrawable(view.getResources().getDrawable(R.drawable.ic_attach_file_grey_24dp));
                    break;
                }
                //    System.out.println(attachs.html() + );
                Pattern p = Pattern.compile("\\{(.*)\\}");
                Matcher m = p.matcher(attachs.attr("onclick"));
                while(m.find()) {
                    JSONObject reader = null;
                    try {
                        reader = new JSONObject(m.group().replace("queue", "\"queue\""));
                        JSONObject response = reader.getJSONObject("temp");

                        String last_element = Utils.getLastElement(response.keys());
                        String base = response.getString("base");
                        JSONArray intemp = response.optJSONArray(last_element);
                        String url = (String) intemp.get(0);

                        if (url.startsWith("https://")) {
                            Picasso.with(view.getContext()).load(url + ".jpg").into(img);
                        } else {
                            Picasso.with(view.getContext()).load(base + url + ".jpg").into(img);
                        }

                        txt_name.setText(R.string.type_image);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            break;
            case 1:
                Element info = mGroups.get(i).get(i1);
                String iname = info.select("div.bt_report_one_info_row_label").text();

                info.select("div.bugtracker_no_device").remove();
                String isub_name = (info.select("div.bt_report_one_info_row_value").hasText() ? info.select("div.bt_report_one_info_row_value").text() : "Нет устройств");

                TextView tname = view.findViewById(R.id.txt_name);
                TextView tsub_name = view.findViewById(R.id.txt_sub_name);

                tname.setText(iname);
                tsub_name.setText(isub_name);
                break;
            case 2:
                Element comments = mGroups.get(i).get(i1);

                Elements content = comments.select("div.bt_report_cmt_content");
                String name_a = content.select("div.bt_report_cmt_author").text();
                String desc_a = content.select("div.bt_report_cmt_text").html();
                String time_a = content.select("div.bt_report_cmt_info").text();
                FrameLayout fl = view.findViewById(R.id.frame_meta);
                if(content.select("div.bt_report_cmt_meta").hasText()) {
                    LayoutInflater inflater = LayoutInflater.from(view.getContext()); // or (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View metaLayout = inflater.inflate(R.layout.bug_comment_meta, null);
                    TextView tv = metaLayout.findViewById(R.id.txt_tag);
                    tv.setText(Utils.trimTrailingWhitespace(Html.fromHtml(content.select("div.bt_report_cmt_meta").html())));
                    fl.addView(metaLayout);
                }
                String url_avatar_a = comments.select("img.bt_report_cmt_img").attr("src");
                TextView name = view.findViewById(R.id.txt_author);
                TextView desc = view.findViewById(R.id.txt_desc);
                TextView time = view.findViewById(R.id.txt_time);
                CircleImageView civ = view.findViewById(R.id.img_author);

                if(url_avatar_a.contains("support")){
                    desc.setTextColor(Color.parseColor("#0d1430"));
                }
            //    desc.setAutoLinkMask(Linkify.WEB_URLS);

                name.setText(name_a);
                desc.setText(Html.fromHtml((desc_a == null ? "" : desc_a)));
                time.setText(time_a);
                if (!url_avatar_a.startsWith("http")){
                    Picasso.with(view.getContext()).load("https://vk.com"+url_avatar_a).into(civ);
                } else {
                    Picasso.with(view.getContext()).load(url_avatar_a).into(civ);
                }

                break;
            default:
                break;
        }

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }


}
