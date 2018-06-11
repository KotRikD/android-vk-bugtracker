package ru.kotrik.bugtracker.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.kotrik.bugtracker.R;

public class AboutAppAdapter extends RecyclerView.Adapter<AboutAppAdapter.ViewHolder>  {

    Context ctx;

    public AboutAppAdapter(Context ctx){
        this.ctx = ctx;
    }

    @Override
    public AboutAppAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.about_app_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AboutAppAdapter.ViewHolder holder, int position) {
        switch (position) {
            case 0:
                holder.text.setText(R.string.testmem);
                break;
            case 1:
                holder.text.setText(R.string.feedback);
                break;
            case 2:
                holder.text.setText(R.string.chat_discussion);
                break;
            case 3:
                holder.text.setText(R.string.chat_flood);
                break;
            case 4:
                holder.text.setText(R.string.group_vk);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView text;

        public ViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch(getAdapterPosition()) {
                case 0:
                    Intent iTestmem = new Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse("https://vk.com/testmem"));
                    ctx.startActivity(iTestmem);
                    break;
                case 1:
                    Intent iFeedback = new Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse("https://vk.me/id311572436"));
                    ctx.startActivity(iFeedback);
                    break;
                case 2:
                    Intent iDiscussion= new Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse("https://vk.me/join/AJQ1d3puuANHMxWPjPJA9quC"));
                    ctx.startActivity(iDiscussion);
                    break;
                case 3:
                    Intent iFlood = new Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse("https://vk.me/join/AJQ1dwPuugM8NqecgW1UY2LP"));
                    ctx.startActivity(iFlood);
                    break;
                case 4:
                    Intent iGroup = new Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse("https://vk.com/btandroid"));
                    ctx.startActivity(iGroup);
                    break;
            }
        }
    }
}
