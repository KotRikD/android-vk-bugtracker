package ru.kotrik.bugtracker.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ru.kotrik.bugtracker.Models.NewItem;
import ru.kotrik.bugtracker.R;

public class TagNewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<NewItem> items;

    public TagNewAdapter(ArrayList<NewItem> itemss) {
        this.items = itemss;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == 0) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.vk_tag_gray, parent, false);
            return new NotActivatedViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.vk_tag_blue, parent, false);
            return new ActivatedViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof  NotActivatedViewHolder) {
            ((NotActivatedViewHolder)holder).name.setText(items.get(position).name);
        } else if(holder instanceof  ActivatedViewHolder){
            ((ActivatedViewHolder)holder).name.setText(items.get(position).name);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(items.get(position).activated) {
            return 1;
        } else {
            return 0;
        }
    }

    class NotActivatedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView name;
        public NotActivatedViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txt_tag);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            NewItem item = items.get(getAdapterPosition());
            if(item.activated) {
                item.activated = false;
            } else {
                item.activated = true;
            }
            notifyDataSetChanged();
        }
    }

    class ActivatedViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {
        public TextView name;
        public ActivatedViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txt_tag);
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View view) {
            NewItem item = items.get(getAdapterPosition());
            if(item.activated) {
                item.activated = false;
            } else {
                item.activated = true;
            }
            notifyDataSetChanged();
        }
    }
}
