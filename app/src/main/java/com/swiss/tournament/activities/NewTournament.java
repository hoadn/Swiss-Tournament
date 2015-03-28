package com.swiss.tournament.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.swiss.tournament.R;
import com.swiss.tournament.database.SqliteController;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import be.md.swiss.Player;
import be.md.swiss.Tournament;
import be.md.swiss.pairing.Round;


public class NewTournament extends Activity {

    public static final String TAG = NewTournament.class.getSimpleName();

    private static String tournament_Name;
    private static int rounds_Number;
    private static ArrayList<Player> players = new ArrayList<>();
    private static PlayerAdapter adapter;
    private static ListView listView;
    private static Tournament tournament;
    private static SqliteController controller;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tournament);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        final EditText Tname = (EditText) findViewById(R.id.editText2);
        final EditText Nround = (EditText) findViewById(R.id.editText4);
        final Button newPlayer = (Button) findViewById(R.id.button6);
        final Button done = (Button) findViewById(R.id.button5);
        final Button startRound1 = (Button) findViewById(R.id.startround1);
        final TextView tournamentPlayers = (TextView) findViewById(R.id.tournamentSaved);
        final TextView tournamentinfo = (TextView) findViewById(R.id.tournament_info);

        list_Players();

        controller = new SqliteController(this);

        Tname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(Tname, InputMethodManager.SHOW_IMPLICIT);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }
        });


        Nround.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(Nround, InputMethodManager.SHOW_IMPLICIT);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }
        });


        newPlayer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                show_keyboard();

                final AlertDialog.Builder alert = new AlertDialog.Builder(NewTournament.this);
                final LinearLayout dialog_layout = new LinearLayout(NewTournament.this);
                dialog_layout.setOrientation(LinearLayout.VERTICAL);

                final EditText first_Name_Input = new EditText(NewTournament.this);
                first_Name_Input.setHint("first name");
                final EditText last_Name_Input = new EditText(NewTournament.this);
                last_Name_Input.setHint("last name");
                final EditText rating_Input = new EditText(NewTournament.this);
                rating_Input.setHint("rating, default is 1200");


                dialog_layout.addView(first_Name_Input);
                dialog_layout.addView(last_Name_Input);
                dialog_layout.addView(rating_Input);
                rating_Input.setInputType(InputType.TYPE_CLASS_NUMBER);

                alert.setTitle("Add New Player");
                alert.setView(dialog_layout);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            int rating = 0;

                            String fName = String.valueOf(first_Name_Input.getText());
                            String lname = String.valueOf(last_Name_Input.getText());
                            if (String.valueOf(rating_Input.getText()).trim().equals("")) {
                                rating = 1200;
                            } else {
                                rating = Integer.parseInt(String.valueOf(rating_Input.getText()));
                            }

                            if (!fName.equals("") || !lname.equals("") || (!fName.equals("") && !lname.equals(""))) {
                                Player player = Player.createPlayerWithFirstnameLastname(fName, lname);
                                player.setRating(rating);
                                players.add(player);
                                Toast.makeText(NewTournament.this, fName + " added successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(NewTournament.this, "No Player added", Toast.LENGTH_SHORT).show();
                            }

                            dialog_layout.removeAllViewsInLayout();
                            dialog_layout.removeAllViews();
                            list_Players();

                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Exception caught", e);
                            Toast.makeText(NewTournament.this, "Invalid Rating!", Toast.LENGTH_LONG).show();
                            hideSoftKeyboard(NewTournament.this);
                        }
                        hideSoftKeyboard(NewTournament.this);
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog_layout.removeAllViewsInLayout();
                        dialog_layout.removeAllViews();
                        hideSoftKeyboard(NewTournament.this);
                    }
                });

                alert.show();
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    tournament_Name = (Tname.getText().toString() + "_" + get_Time()).replaceAll("\\s+", "_");

                    rounds_Number = Integer.parseInt(Nround.getText().toString());

                    if (rounds_Number <= 1 || rounds_Number >= players.size() || players.size() < 4 || players.size() > 30) {
                        Toast.makeText(NewTournament.this, getString(R.string.roundsformatting), Toast.LENGTH_LONG).show();

                    } else {
                        done.setVisibility(View.INVISIBLE);
                        newPlayer.setVisibility(View.INVISIBLE);
                        Tname.setVisibility(View.INVISIBLE);
                        Nround.setVisibility(View.INVISIBLE);
                        listView.setVisibility(View.INVISIBLE);
                        tournamentPlayers.setText(getPlayersNames());
                        tournamentinfo.setText(" " + tournament_Name.toUpperCase() + "\n" + " Number of Rounds: " + rounds_Number + "\n");
                        tournamentPlayers.setMovementMethod(new ScrollingMovementMethod());
                        tournamentPlayers.setVisibility(View.VISIBLE);
                        tournamentinfo.setVisibility(View.VISIBLE);
                        startRound1.setVisibility(View.VISIBLE);

                        tournament = Tournament.createTournament(rounds_Number);
                        tournament.setName(tournament_Name);
                        tournament.setPlayers(new HashSet<>(players));
                    }


                    System.out.println(players.toString());

                } catch (NumberFormatException e) {
                    Toast.makeText(NewTournament.this, "Make sure Number of rounds is a valid Number.", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Exception caught", e);
                }
            }

        });


        startRound1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Round round1 = tournament.pairNextRound();

                boolean success = controller.insert_new_Tournament(tournament, round1, rounds_Number);
                if (!success) {
                    Toast.makeText(NewTournament.this, "ERROR: Tournament name already exist! Try different name", Toast.LENGTH_LONG).show();
                }
                controller.close();
                Intent intent = new Intent(NewTournament.this, Rounds.class);
                intent.putExtra("tournament", tournament_Name);
                round1 = null;
                tournament = null;
                for (Player p : players) {
                    p.setPoints(0);
                }
                startActivity(intent);
                finish();
            }
        });


    }

    // ---------- SUPPORT ----------

    private String get_Time() {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return month + 1 + "_" + day + "_" + year;
    }

    public void list_Players() {
        ArrayList<String> players_Names = new ArrayList<String>();
        for (Player p : players) {
            players_Names.add(p.getFirstname() + " " + p.getLastname());
        }
        adapter = new PlayerAdapter(this, 0x7f03001c, players);
        listView = (ListView) findViewById(R.id.player_list_view);
        listView.setAdapter(adapter);
    }

    public void removePlayer(View v) {
        Player player = (Player) v.getTag();
        players.remove(player);
        adapter.remove(player);
    }


    public String getPlayersNames() {
        int counter = 1;
        String names_list = "";

        for (Player p : players) {
            names_list += " " + counter + ") " + p.getFirstname().toUpperCase() + "    " + p.getLastname().toUpperCase() + "\n";
            names_list += "----------------------------------------------------------------" + "\n";
            counter++;
        }
        return names_list;
    }

    public void show_keyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void hideSoftKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

}