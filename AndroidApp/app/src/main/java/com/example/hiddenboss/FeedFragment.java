package com.example.hiddenboss;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
//FeedFragments is the fragment that holds the feedAdapter
public class FeedFragment extends Fragment implements FeedAdapter.listButtonListener{
    //The global variables used for the fragment
    private static final String TAG = "FeedFragment";
    private FeedAdapter.listButtonListener mListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container,
                             @NonNull Bundle savedInstanceState) {

        //The recyclerview for the feed is created with the tournament objects from the main activity
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        Log.d(TAG, "OnCreateView: Started");

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        Bundle receivedFeed = this.getArguments();
        assert receivedFeed != null;
        ArrayList<Tournaments> tournaments = receivedFeed.getParcelableArrayList("feedTournaments");

        FeedAdapter adapter = new FeedAdapter(tournaments,view.getContext(), mListener);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        Log.d(TAG, "initRecyclerView: finished recyclerview.");

        return view;
    }

    @Override
    public void onAttach(Context context) {
        //When the fragment is attached the Listener from the feedAdapter is assigned a value
        super.onAttach(context);
        if (context instanceof FeedAdapter.listButtonListener) {
            mListener = (FeedAdapter.listButtonListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement listButtonListener");
        }
    }

    @Override
    public void onDetach() {
        //When the fragment is detached it sets the listener to null
        super.onDetach();
        mListener = null;
    }

    //Methods required because the FeedAdapter has a interface containing them. They are unused in the fragment itself
    @Override
    public void tournamentInfo(Tournaments tournament) { }

    @Override
    public void openLogin() {}
}
