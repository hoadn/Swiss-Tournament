package com.swiss.tournament.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.swiss.tournament.R;
import com.swiss.tournament.database.SqliteController;

import java.util.ArrayList;


public class Archive extends Activity {

    private static TournametAdapter adapter;
    private static SqliteController controller;
    private static SQLiteDatabase database;
    private static ArrayList<String> tournaments;
    private static TextView no_tournaments;
    private static AlertDialog deleteDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        no_tournaments = (TextView) findViewById(R.id.no_tournaments);

        controller = new SqliteController(this);
        database = controller.getWritableDatabase();
        tournaments = new ArrayList<>();

        Cursor c = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                tournaments.add(c.getString(c.getColumnIndex("name")));
                c.moveToNext();
            }
        }
        database.close();
        controller.close();
        tournaments.remove("android_metadata");
        adapter = new TournametAdapter(this, R.layout.tournamet_names_layout, tournaments);
        ListView listView = (ListView) findViewById(R.id.archive);
        if (tournaments.size() == 0) {
            no_tournaments.setVisibility(View.VISIBLE);
        } else {
            listView.setAdapter(adapter);
        }

    }

    public void removeTournament(final View v) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete Tournament?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        String tournament_name = (String) v.getTag();
                        controller.deleteTournament(tournament_name);
                        adapter.remove(tournament_name);
                        if (tournaments.size() == 0) {
                            no_tournaments.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteDialog.dismiss();
                    }
                });
        deleteDialog = builder.create();
        deleteDialog.show();

    }

    public void selectTournament(View v) {
        String tournament_name = (String) v.getTag();
        database.close();
        controller.close();
        Intent intent = new Intent(this, Rounds.class);
        intent.putExtra("tournament", tournament_name);
        startActivity(intent);
    }
}