package ru.kotrik.bugtracker.Adapters;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ru.kotrik.bugtracker.Models.Bug;
import ru.kotrik.bugtracker.R;
import ru.kotrik.bugtracker.Helpers.Utils;
import ru.kotrik.bugtracker.VKActivities.ActivityBug;

/**
 * Created by kotoriku on 21.03.2018.
 */

public class BugAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Bug> bugs;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    public BugAdapter(ArrayList<Bug> bugs, String founded_reports) {
        this.bugs = bugs;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_HEADER) {
            TextView founds_reports = new TextView(viewGroup.getContext());
            founds_reports.setTextColor(Color.BLACK);
            founds_reports.setPadding(
                    Utils.dpAsPixel(viewGroup.getContext(), 15),
                    Utils.dpAsPixel(viewGroup.getContext(), 10),
                    0,
                    Utils.dpAsPixel(viewGroup.getContext(), 10)
            );
            founds_reports.setTextAppearance(viewGroup.getContext(), R.style.TextAppearance_AppCompat_Body2);

            return new HeaderViewHolder(founds_reports);
        } else if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tracker_item, viewGroup, false);
            return new ViewHolder(v);
        }
        throw new RuntimeException("No match for " + viewType + ".");
    }



    @Override
    public int getItemViewType(int position) {
        if(position == 0) {
            return 0;
        } else {
            return 1;
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).updateTitle();
        }
        else if(holder instanceof ViewHolder) {
            Bug p = bugs.get(position);
            ((ViewHolder) holder).name.setText(p.name);
            ((ViewHolder) holder).type.setText(p.status);
            ((ViewHolder) holder).time.setText(p.time);
            TagsAdapter ta = new TagsAdapter(p.tags);
            ((ViewHolder) holder).rv_tags.setFocusable(false);
            ((ViewHolder) holder).rv_tags.setAdapter(ta);
        }


    }

    @Override
    public int getItemCount() {
        return bugs.size();
    }


    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView text;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            text = ((TextView) itemView);
        }
        public void updateTitle(){
            int size =  getItemCount()-1;
            text.setText("НАЙДЕНО " + size + " ОТЧЁТОВ");
        }
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView name;
        public TextView type;
        public TextView time;
        public RecyclerView rv_tags;

        public ViewHolder(View itemView)  {
            super(itemView);

             name = itemView.findViewById(R.id.txt_name);
             type = itemView.findViewById(R.id.txt_status);
             time = itemView.findViewById(R.id.txt_time);
             rv_tags = itemView.findViewById(R.id.sys_tags);
             rv_tags.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
             rv_tags.setHasFixedSize(true);

             itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            Bug p = bugs.get(getAdapterPosition());
            Intent i2 = new Intent(view.getContext(), ActivityBug.class);
            i2.putExtra("url", p.link);
            view.getContext().startActivity(i2);
        }
    }

}
