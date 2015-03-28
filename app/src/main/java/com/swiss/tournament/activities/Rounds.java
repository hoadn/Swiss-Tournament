package com.swiss.tournament.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.swiss.tournament.R;
import com.swiss.tournament.database.SqliteController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import be.md.swiss.Pairing;
import be.md.swiss.PairingImpl;
import be.md.swiss.Player;
import be.md.swiss.Tournament;
import be.md.swiss.pairing.Round;
import be.md.swiss.pairing.SwissEngine;


public class Rounds extends Activity {

    private static SqliteController controller;
    private static SQLiteDatabase database;

    private static Set<String> rounds = new HashSet<>();
    private static Set<Player> byes = new HashSet<>();
    private static Set<Pairing> doneSoFar = new HashSet<>();

    private static Tournament tournament;
    private static SwissEngine engine = new SwissEngine();
    private static RoundAdapter adapter;

    private static String tournament_name;
    private static String round;
    private static int totalRounds = 0;
    private static int roundIndex = 1;
    private static float lastX;
    private static ViewFlipper viewFlipper;
    private static AlertDialog levelDialog;
    private static ListView listView;
    private static TextView roundNum;
    boolean allPlayed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rounds_flipper);

        viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
        listView = (ListView) findViewById(R.id.roundlist);
        roundNum = (TextView) findViewById(R.id.round1);
        Button standings = (Button) findViewById(R.id.standings);
        roundNum.setText("Round 1");

        Intent intent = getIntent();
        tournament_name = intent.getStringExtra("tournament");

        controller = new SqliteController(this);
        database = controller.getWritableDatabase();

        // read all pairings from database for Round 1
        renderPairings(listView, 1);
        Toast.makeText(Rounds.this, "Swipe right to pair/view next Round --->", Toast.LENGTH_SHORT).show();
        getRoundsSize();
        allPlayed = isAllPlayed(roundIndex);


        standings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialoge();
            }

        });
    }

    // method to read pairings from database and render on the list
    private void renderPairings(ListView listView, int roundNumber) {
        List<PairingImpl> pairing = getPairsByRound(roundNumber);

        adapter = new RoundAdapter(this, R.layout.roundstable, pairing);
        listView.setAdapter(adapter);
        loadEngine();
    }

    // Method to handle touch event like left to right swap and right to left swap
    public boolean onTouchEvent(MotionEvent touchevent) {

        switch (touchevent.getAction()) {
            // when user first touches the screen to swap
            case MotionEvent.ACTION_DOWN: {
                lastX = touchevent.getX();
                break;
            }
            case MotionEvent.ACTION_UP: {
                float currentX = touchevent.getX();

                // if right to left swipe on screen
                if (lastX > currentX) {
                    boolean roundsExist = rounds.contains(String.valueOf(1 + roundIndex));

                    if (allPlayed == false) {
                        Toast.makeText(Rounds.this, "Not all results submitted to start new round", Toast.LENGTH_SHORT).show();
                        break;
                    } else if (totalRounds == roundIndex && (allPlayed)) {
                        Toast.makeText(Rounds.this, " The Tournament is finished see Standings!", Toast.LENGTH_SHORT).show();
                        break;
                    } else if ((allPlayed && !roundsExist)) {

                        Set<Player> players = getPlayersList(roundIndex);
                        roundIndex++;
                        PairingImpl byePair = null;

                        tournament = Tournament.createTournament(totalRounds);
                        tournament.setActiveRound(roundIndex);
                        tournament.setEngine(engine);

                        if (players.size() % 2 == 1) {
                            byePair = filterBye(players);
                        }

                        tournament.setPlayers(players);

                        // start next Round
                        final Round newRound = tournament.pairNextRoundNotFirst();

                        if(newRound.pairings.isEmpty()){
                            Toast.makeText(Rounds.this, "Paired Failed", Toast.LENGTH_SHORT).show();
                            break;
                        }

                        controller.insert_new_round(tournament_name, newRound, byePair, totalRounds, roundIndex);


                        Toast.makeText(Rounds.this, "Paired Successfully", Toast.LENGTH_SHORT).show();

                        if (!database.isOpen()) {
                            database = controller.getWritableDatabase();
                        }
                        roundNum.setText("Round " + roundIndex);
                        renderPairings(listView, roundIndex);
                        allPlayed = isAllPlayed(roundIndex);
                        break;

                    } else if (allPlayed && roundsExist) {
                        if (!database.isOpen()) {
                            database = controller.getWritableDatabase();
                        }
                        roundIndex++;
                        roundNum.setText("Round " + roundIndex);
                        renderPairings(listView, roundIndex);
                        allPlayed = isAllPlayed(roundIndex);
                        break;
                    }

                }

                // if left to right swipe on screen
                if (lastX < currentX) {
                    if (roundIndex > 1) {
                        if (!database.isOpen()) {
                            database = controller.getWritableDatabase();
                        }
                        --roundIndex;
                        roundNum.setText("Round " + roundIndex);
                        renderPairings(listView, roundIndex);
                        allPlayed = isAllPlayed(roundIndex);
                    } else {
                        break;
                    }
                }
                break;
            }
        }
        getRoundsSize();
        return false;
    }

    // input result on a pair of players
    public void selectPair(View v) {

        final PairingImpl Pair = (PairingImpl) (v.getTag());

        final CharSequence[] items = {Pair.getWhite().getfullname(), Pair.getBlack().getfullname(), "Draw"};

        // Creating and Building the Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Who Won?");
        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        Pair.whiteWins();
                        controller.updateScore(Pair, tournament_name, database);
                        System.out.println(Pair.getWhite().getPoints());
                        roundNum.setText("Round " + roundIndex);
                        renderPairings(listView, roundIndex);
                        break;
                    case 1:
                        Pair.blackWins();
                        controller.updateScore(Pair, tournament_name, database);
                        System.out.println(Pair.getBlack().getPoints());
                        roundNum.setText("Round " + roundIndex);
                        renderPairings(listView, roundIndex);
                        break;
                    case 2:
                        Pair.draw();
                        controller.updateScore(Pair, tournament_name, database);
                        System.out.println(Pair.getWhite().getPoints());
                        System.out.println(Pair.getBlack().getPoints());
                        roundNum.setText("Round " + roundIndex);
                        renderPairings(listView, roundIndex);
                        break;
                }
                allPlayed = isAllPlayed(roundIndex);
                levelDialog.dismiss();

                if(allPlayed && (totalRounds != roundIndex)){
                    Toast.makeText(Rounds.this, "Swipe right to pair/view next Round --->", Toast.LENGTH_SHORT).show();
                }
            }
        });
        levelDialog = builder.create();
        levelDialog.show();
    }

    public List<PairingImpl> getPairsByRound(int roundNumber) {
        List<PairingImpl> pairing = new ArrayList<>();


        Cursor c = database.rawQuery("SELECT * FROM " + tournament_name + " WHERE Round = " + roundNumber, null);
        if (c != null) {
            //Move cursor to first row
            if (c.moveToFirst()) {
                do {
                    //Get Round from Cursor
                    round = c.getString(c.getColumnIndex("Round"));
                    totalRounds = Integer.parseInt(c.getString(c.getColumnIndex("total")));
                    String white = c.getString(c.getColumnIndex("white"));
                    String white_score = c.getString(c.getColumnIndex("score_w"));
                    String white_result = c.getString(c.getColumnIndex("white_result"));

                    float sb_white = c.getFloat(c.getColumnIndex("sb_white"));
                    int white_rating = c.getInt(c.getColumnIndex("white_rating"));


                    String black = c.getString(c.getColumnIndex("black"));
                    String black_score = c.getString(c.getColumnIndex("score_b"));
                    String black_result = c.getString(c.getColumnIndex("black_result"));

                    float sb_black = c.getFloat(c.getColumnIndex("sb_black"));
                    int black_rating = c.getInt(c.getColumnIndex("black_rating"));

                    Player White_Player = Player.createPlayerWithFirstnameLastname(white, "");
                    White_Player.setfullname(white);
                    Player Black_Player = Player.createPlayerWithFirstnameLastname(black, "");
                    Black_Player.setfullname(black);

                    White_Player.setPoints(Integer.parseInt(white_score));
                    White_Player.addSonnebornBerner(sb_white);

                    Black_Player.setPoints(Integer.parseInt(black_score));
                    Black_Player.addSonnebornBerner(sb_black);

                    White_Player.setRating(white_rating);
                    Black_Player.setRating(black_rating);


                    PairingImpl pair = PairingImpl.createPairing(White_Player, Black_Player);

                    if (white_result.equals("0.5")) {
                        pair.draw();
                    } else if (black_result.equals("1")) {
                        pair.blackWins();
                    } else if (white_result.equals("1")) {
                        pair.whiteWins();
                    }

                    pairing.add(pair);

                } while (c.moveToNext()); //Move to next row
            }
        }
        return pairing;
    }


    public void loadEngine() {
        Set<PairingImpl> pairing = new HashSet<>();

        Cursor c = database.rawQuery("SELECT * FROM " + tournament_name, null);
        if (c != null) {
            //Move cursor to first row
            if (c.moveToFirst()) {
                do {
                    //Get Round from Cursor
                    String white = c.getString(c.getColumnIndex("white"));
                    String white_score = c.getString(c.getColumnIndex("score_w"));
                    String white_result = c.getString(c.getColumnIndex("white_result"));

                    float sb_white = c.getFloat(c.getColumnIndex("sb_white"));
                    int white_rating = c.getInt(c.getColumnIndex("white_rating"));


                    String black = c.getString(c.getColumnIndex("black"));
                    String black_score = c.getString(c.getColumnIndex("score_b"));
                    String black_result = c.getString(c.getColumnIndex("black_result"));

                    float sb_black = c.getFloat(c.getColumnIndex("sb_black"));

                    Player White_Player = Player.createPlayerWithFirstnameLastname(white, "");
                    White_Player.setfullname(white);
                    Player Black_Player = Player.createPlayerWithFirstnameLastname(black, "");
                    Black_Player.setfullname(black);

                    White_Player.setPoints(Integer.parseInt(white_score));
                    White_Player.addSonnebornBerner(sb_white);

                    Black_Player.setPoints(Integer.parseInt(black_score));
                    Black_Player.addSonnebornBerner(sb_black);
                    int black_rating = c.getInt(c.getColumnIndex("black_rating"));

                    White_Player.setRating(white_rating);
                    Black_Player.setRating(black_rating);

                    PairingImpl pair = PairingImpl.createPairing(White_Player, Black_Player);

                    if (white_result.equals("0.5")) {
                        pair.draw();
                    } else if (black_result.equals("1")) {
                        pair.blackWins();
                    } else if (white_result.equals("1")) {
                        pair.whiteWins();
                    }

                    pairing.add(pair);
                } while (c.moveToNext()); //Move to next row
            }
        }
        doneSoFar.addAll(pairing);
        System.out.println("all pairing done : " + doneSoFar.toString());
        engine.setAllPairingsDoneSoFar(doneSoFar);
    }

    public boolean isAllPlayed(int roundNumber) {
        Cursor c = database.rawQuery("SELECT * FROM " + tournament_name + " WHERE Round = " + roundNumber, null);
        if (c != null) {
            //Move cursor to first row
            if (c.moveToFirst()) {
                do {
                    //Get Round from Cursor
                    String played = c.getString(c.getColumnIndex("played"));
                    if (played.equals("0")) {
                        return false;
                    }
                } while (c.moveToNext()); //Move to next row
            }
        }
        return true;
    }

    public void getRoundsSize() {
        Cursor c = database.rawQuery("SELECT * FROM " + tournament_name, null);
        if (c != null) {
            //Move cursor to first row
            if (c.moveToFirst()) {
                do {
                    //Get Round from Cursor
                    round = c.getString(c.getColumnIndex("Round"));
                    rounds.add(round);
                } while (c.moveToNext()); //Move to next row
            }
        }
    }

    public Set<Player> getPlayersList(int roundNumber) {
        Set<Player> players = new HashSet<>();
        Cursor c = database.rawQuery("SELECT * FROM " + tournament_name + " WHERE Round = " + roundNumber, null);
        if (c != null) {
            //Move cursor to first row
            if (c.moveToFirst()) {
                do {
                    String white = c.getString(c.getColumnIndex("white"));
                    String white_score = c.getString(c.getColumnIndex("score_w"));
                    int white_rating = c.getInt(c.getColumnIndex("white_rating"));

                    float sb_white = c.getFloat(c.getColumnIndex("sb_white"));
                    String black = c.getString(c.getColumnIndex("black"));
                    String black_score = c.getString(c.getColumnIndex("score_b"));
                    int black_rating = c.getInt(c.getColumnIndex("black_rating"));

                    float sb_black = c.getFloat(c.getColumnIndex("sb_black"));

                    Player White_Player = Player.createPlayerWithFirstnameLastname(white, "");
                    White_Player.setfullname(white);
                    Player Black_Player = Player.createPlayerWithFirstnameLastname(black, "");
                    Black_Player.setfullname(black);

                    White_Player.setPoints(Integer.parseInt(white_score));
                    Black_Player.setPoints(Integer.parseInt(black_score));

                    White_Player.addSonnebornBerner(sb_white);
                    Black_Player.addSonnebornBerner(sb_black);

                    White_Player.setRating(white_rating);
                    Black_Player.setRating(black_rating);

                    if (Black_Player.getfullname().contains("BYE")) byes.add(White_Player);

                    players.add(White_Player);

                    if (!Black_Player.getfullname().contains("BYE"))
                        players.add(Black_Player);

                } while (c.moveToNext()); //Move to next row
            }
        }
        return players;
    }

    private PairingImpl filterBye(Set<Player> players) {
        List<Player> Players = new ArrayList<>(players);
        PairingImpl createdByePair = null;

        Player Bye = Player.createPlayerWithFirstnameLastname("BYE", "");
        Bye.setPoints(0);
        Bye.setRating(0);
        Bye.addSonnebornBerner(0);

        Collections.sort(Players, new PlayerCompare());
        for (Player p : Players) {

            if (byes.contains(p)) continue;

            createdByePair = PairingImpl.createPairing(p, Bye);
            players.remove(p);
            return createdByePair;
        }
        return createdByePair;
    }

    private void showDialoge() {
        List<Player> sorted = new ArrayList<>();
        sorted.addAll(getPlayersList(roundIndex));
        Collections.sort(sorted, new PlayerCompare());
        Collections.reverse(sorted);

        List<String> sortedPlayers = new ArrayList<>();
        sortedPlayers.add("Name                 Score       Sb       Rating");

        for (Player p : sorted) {
            sortedPlayers.add((sorted.indexOf(p) + 1) + "- " + p.getfullname() + "\n                            " + (float) (p.getPoints()) / 10 + "           " + p.getSonnebornBerner() + "        " + p.getRating());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Standings");

        ListView standingsList = new ListView(this);

        StandingsAdapter<String> modeAdapter = new StandingsAdapter<>(this, android.R.layout.simple_list_item_1, sortedPlayers);
        standingsList.setAdapter(modeAdapter);

        builder.setView(standingsList);
        final Dialog dialog = builder.create();

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        database.close();
        controller.close();
        roundIndex = 1;
        rounds.clear();
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
        finish();
    }

    static class PlayerCompare implements Comparator {

        public int compare(Object o1, Object o2) {

            Player p1 = (Player) o1;
            Player p2 = (Player) o2;

            if (p1.getPoints() > p2.getPoints()) return 1;
            if (p1.getPoints() < p2.getPoints()) return -1;

            if (p1.getSonnebornBerner() > p2.getSonnebornBerner()) return 1;
            if (p1.getSonnebornBerner() < p2.getSonnebornBerner()) return -1;

            if (p1.getRating() > p2.getRating()) return 1;
            if (p1.getRating() < p2.getRating()) return -1;

            return 0;
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }
    }
}
