package com.swiss.tournament.activities;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import be.md.swiss.Player;
import com.swiss.tournament.R;

/**
 * Created by JHILAN on 11/1/14.
 */
public class PlayerAdapter extends ArrayAdapter {


    private List<Player> players;
    private int layoutResourceId;
    private Context context;

    public PlayerAdapter(Context context, int layoutResourceId, List<Player> players) {
        super(context, layoutResourceId, players);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.players = players;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        PlayerHolder holder = null;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(layoutResourceId, parent, false);

        holder = new PlayerHolder();
        holder.player = players.get(position);
        holder.removeButton = (ImageButton) row.findViewById(R.id.player_remove);
        holder.removeButton.setTag(holder.player);

        holder.name = (TextView) row.findViewById(R.id.player_name);
        holder.rating = (TextView) row.findViewById(R.id.player_rating);

        row.setTag(holder);

        setupItem(holder);
        return row;
    }

    private void setupItem(PlayerHolder holder) {
        holder.name.setText("  " + holder.player.getFirstname() + "  " + holder.player.getLastname());
        holder.rating.setText(String.valueOf(holder.player.getRating()));
    }

    public static class PlayerHolder {
        Player player;
        TextView name;
        TextView rating;
        ImageButton removeButton;
    }
}