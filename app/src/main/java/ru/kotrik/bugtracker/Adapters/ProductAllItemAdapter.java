package ru.kotrik.bugtracker.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import ru.kotrik.bugtracker.Helpers.Async.CookedDocument;
import ru.kotrik.bugtracker.Helpers.Async.CookedPostDocument;
import ru.kotrik.bugtracker.Helpers.Callback;
import ru.kotrik.bugtracker.Helpers.Exceptions.AccessDeniedBtException;
import ru.kotrik.bugtracker.Helpers.Exceptions.NoInternetException;
import ru.kotrik.bugtracker.Models.ProductItemAll;
import ru.kotrik.bugtracker.R;

public class ProductAllItemAdapter extends ArrayAdapter<ProductItemAll> {

    private Pattern acceptLicense = Pattern.compile("BugTracker\\.joinProduct\\((.*)\\);");
    private Pattern decileLicense = Pattern.compile("BugTracker\\.deleteLicenceRequest\\((.*)\\);");
    private Callback mCallback;
    Button request;
    public ProductAllItemAdapter( Context context, int resource, ArrayList<ProductItemAll> items, Callback mCallback) {
        super(context, resource, items);
        this.mCallback = mCallback;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.products_tabs_all_item, null);
        }

        final ProductItemAll p = getItem(position);

        TextView name =  v.findViewById(R.id.txt_name);
        CircleImageView civ = v.findViewById(R.id.img_avatar);
        request = v.findViewById(R.id.btn_send);
        if (p != null) {
            if(p.isRequest) {
                request.setText(R.string.request_accept);
            } else {
                request.setText(R.string.decile_request);
            }
            if (p.btHash == null) {
                request.setVisibility(View.INVISIBLE);
                notifyDataSetChanged();
            } else {
                request.setVisibility(View.VISIBLE);
                notifyDataSetChanged();
            }


            Picasso.with(getContext()).load(p.img).into(civ);
            name.setText(p.name);

           request.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Map<String, String> data = new HashMap<String, String>();
                    final ProductItemAll pia = p;
                    if(pia.isRequest) {
                        data.put("act", "a_join_product");
                    } else {
                        data.put("act", "a_delete_licence_request");
                    }

                    data.put("id", pia.id);
                    data.put("reload", "0");
                    data.put("hash", pia.btHash);


                    new CookedPostDocument(view.getContext(), new Callback() {
                        @Override
                        public void onError(Exception e) {
                            if(e instanceof AccessDeniedBtException) {
                                Toast.makeText(getContext(), R.string.error_access_denied, Toast.LENGTH_SHORT).show();
                            }
                            else if(e instanceof NoInternetException) {
                                Toast.makeText(getContext(), R.string.error_no_internet, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onSuccess(Document d) {
                           new CookedDocument(getContext(), mCallback, "https://vk.com/bugtracker?act=products&section=all").execute();
                        }
                    }, "https://vk.com/bugtracker", data).execute();
                }
            });

        }
        return v;
    }
}
