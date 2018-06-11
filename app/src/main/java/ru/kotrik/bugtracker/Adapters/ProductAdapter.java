package ru.kotrik.bugtracker.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ru.kotrik.bugtracker.Models.ReportProductsProfile;
import ru.kotrik.bugtracker.R;

/**
 * Created by kotoriku on 28.03.2018.
 */

public class ProductAdapter extends ArrayAdapter<ReportProductsProfile> {
    public ProductAdapter(Context context, int resource, ArrayList<ReportProductsProfile> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.bug_product, null);
        }

        ReportProductsProfile p = getItem(position);

        TextView name = v.findViewById(R.id.txt_name_product);
        TextView reports = v.findViewById(R.id.txt_reports);
        ImageView logo = v.findViewById(R.id.img_avatar);

        if (p != null) {
            name.setText(p.name_product);
            reports.setText(p.count_reports);
            if(!p.icon_url.isEmpty()) {
                Picasso.with(v.getContext()).load(p.icon_url).into(logo);
            }
        }
        return v;
    }
}
