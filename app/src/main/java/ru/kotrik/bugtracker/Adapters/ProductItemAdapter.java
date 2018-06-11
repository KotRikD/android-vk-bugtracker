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

import ru.kotrik.bugtracker.Models.ProductItem;
import ru.kotrik.bugtracker.R;

public class ProductItemAdapter extends ArrayAdapter<ProductItem> {

    public ProductItemAdapter(Context context, int resource, ArrayList<ProductItem> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.products_tabs_my_item, null);
        }

        ProductItem p = getItem(position);

        TextView name = v.findViewById(R.id.txt_name);
        TextView count = v.findViewById(R.id.txt_status);
        TextView version = v.findViewById(R.id.txt_update);
        ImageView logo = v.findViewById(R.id.img_avatar);

        if (p != null) {
            name.setText(p.name_product);
            count.setText(p.count_of_reports);
            version.setText(p.version);
            if(!p.image_url.isEmpty()) {
                Picasso.with(v.getContext()).load(p.image_url).into(logo);
            }
        }
        return v;
    }
}
