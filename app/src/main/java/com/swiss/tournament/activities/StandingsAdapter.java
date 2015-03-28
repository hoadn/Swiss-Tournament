package com.swiss.tournament.activities;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by JHILAN on 3/7/15.
 */
public class StandingsAdapter<String> extends ArrayAdapter {

    private List<String> players;
    private int layoutResourceId;
    private Context context;


    public StandingsAdapter(Context context, int layoutResourceId, List<String> players) {
        super(context, layoutResourceId, players);
        this.context = context;
        this.players = players;
    }


}
