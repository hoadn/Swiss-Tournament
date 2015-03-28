package com.swiss.tournament.activities;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.swiss.tournament.R;

import java.util.List;

/**
 * Created by JHILAN on 11/5/14.
 */
public class TournametAdapter extends ArrayAdapter {
    private List<String> tournaments;
    private int layoutResourceId;
    private Context context;

    public TournametAdapter(Context context, int layoutResourceId, List<String> tournaments) {
        super(context, layoutResourceId, tournaments);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.tournaments = tournaments;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        TournamentHolder holder = null;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(layoutResourceId, parent, false);

        holder = new TournamentHolder();
        holder.tournament = tournaments.get(position);
        holder.removeButton = (ImageButton) row.findViewById(R.id.tournament_remove);
        holder.removeButton.setTag(holder.tournament);

        holder.name = (TextView) row.findViewById(R.id.tournament_name);
        holder.name.setTag(holder.tournament);
        row.setTag(holder);

        setupItem(holder);
        return row;
    }

    private void setupItem(TournamentHolder holder) {
        holder.name.setText("  " + holder.tournament);
    }

    public static class TournamentHolder {
        String tournament;
        TextView name;
        ImageButton removeButton;
    }
}
