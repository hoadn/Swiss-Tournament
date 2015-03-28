package com.swiss.tournament.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

import com.swiss.tournament.R;


public class Main extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button newTournament = (Button) findViewById(R.id.button5);
        Button Archive = (Button) findViewById(R.id.button2);
        final Intent intent_new = new Intent(this, NewTournament.class);
        final Intent rounds_flipper = new Intent(this, com.swiss.tournament.activities.Archive.class);

        newTournament.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intent_new);
                //finish();
            }
        });


        Archive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(rounds_flipper);
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
