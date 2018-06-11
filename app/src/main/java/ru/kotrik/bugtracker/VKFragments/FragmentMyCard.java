package ru.kotrik.bugtracker.VKFragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.kotrik.bugtracker.Adapters.CardAdapter;
import ru.kotrik.bugtracker.CustomClasses.CustomItemDecoration;
import ru.kotrik.bugtracker.R;

public class FragmentMyCard extends Fragment {

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.my_card_fragment, container, false);
        RecyclerView rv = view.findViewById(R.id.list_rv);

        final Toolbar mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setIcon(R.drawable.ic_vk);

        rv.setLayoutManager(new LinearLayoutManager(view.getContext()));
        rv.addItemDecoration(new CustomItemDecoration(getContext(), 8));
        rv.setAdapter(new CardAdapter());
        return view;
    }
}
