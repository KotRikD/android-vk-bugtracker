package ru.kotrik.bugtracker.Adapters;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


import ru.kotrik.bugtracker.R;

/**
 * Created by kotoriku on 21.03.2018.
 */


public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.ViewHolder> {

    private ArrayList<String> items;

    public TagsAdapter(ArrayList<String> items){
        this.items = items;
    }

    @Override
    public TagsAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tracker_tag, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TagsAdapter.ViewHolder holder, int position) {
        String p = items.get(position);

        holder.name.setText(p);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.txt_tag);
        }
    }
}