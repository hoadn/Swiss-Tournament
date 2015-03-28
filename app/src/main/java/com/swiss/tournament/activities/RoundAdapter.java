package com.swiss.tournament.activities;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import be.md.swiss.PairingImpl;
import com.swiss.tournament.R;

/**
 * Created by JHILAN on 11/9/14.
 */
public class RoundAdapter extends ArrayAdapter {

    private List<PairingImpl> pairings;
    private int layoutResourceId;
    private Context context;

    public RoundAdapter(Context context, int layoutResourceId, List<PairingImpl> pairings) {
        super(context, layoutResourceId, pairings);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.pairings = pairings;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        PairingHolder holder = null;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(layoutResourceId, parent, false);

        holder = new PairingHolder();
        holder.pairings = pairings.get(position);
        holder.black = (TextView) row.findViewById(R.id.black);
        holder.white = (TextView) row.findViewById(R.id.white);
        holder.result = (TextView) row.findViewById(R.id.result);

        holder.white.setTag(holder.pairings);
        holder.black.setTag(holder.pairings);
        holder.result.setTag(holder.pairings);


        row.setTag(holder);

        setupItem(holder);
        return row;
    }

    private void setupItem(PairingHolder holder) {
        String white_result = "0";
        String black_result = "0";

        if (holder.pairings.whiteWon()) {
            white_result = "1";
        } else if (holder.pairings.isDraw()) {
            white_result = ".5";
            black_result = ".5";
        } else if (holder.pairings.isPlayed()) {
            black_result = "1";
        }

        holder.white.setText("  " + holder.pairings.getWhite().getfullname());
        holder.black.setText("  " + holder.pairings.getBlack().getfullname());
        holder.result.setText(String.valueOf("   " + white_result + " - " + black_result));
    }

    public static class PairingHolder {
        PairingImpl pairings;
        TextView white;
        TextView result;
        TextView black;
    }
}
