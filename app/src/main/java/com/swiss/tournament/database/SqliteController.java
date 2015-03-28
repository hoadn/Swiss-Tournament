package com.swiss.tournament.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Set;

import be.md.swiss.Pairing;
import be.md.swiss.PairingImpl;
import be.md.swiss.Player;
import be.md.swiss.Tournament;
import be.md.swiss.pairing.Round;

/**
 * Created by JHILAN on 9/27/14.
 */
public class SqliteController extends SQLiteOpenHelper {

    private static final String Opponent = "black";
    private static final String Score_w = "score_w";
    private static final String Score_b = "score_b";
    private static final String ROUND = "Round";
    private static final String PLAYED = "played";

    private static final String LOGCAT = null;

    public SqliteController(Context applicationcontext) {
        super(applicationcontext, "androidsqlite.db", null, 1);
        Log.d(LOGCAT, "Created");
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.d(LOGCAT, "Tournament Created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int version_old, int current_version) {
        // database.execSQL(CREATE_TOURNAMENT);
        onCreate(database);
    }

    public boolean insert_new_Tournament(Tournament tournament, Round round, int totalRounds) {
        String white = "";
        String black = "";
        Set<Player> players = tournament.getPlayers();
        SQLiteDatabase database = this.getWritableDatabase();
        String create =
                "CREATE TABLE  " + tournament.getName() + " ( total INTEGER , " + ROUND + " INTEGER ,  white VARCHAR(255) , white_result REAL  ,  white_rating INTEGER ,"
                        + Score_w + " INTEGER, sb_white REAL ," + Opponent + " VARCHAR(255) , black_result REAL , black_rating INTEGER , " + Score_b + " INTEGER , sb_black REAL , " +
                        PLAYED + " VARCHAR(255) );";
        try {
            database.execSQL(create);
        } catch (SQLiteException e) {
            return false;
        }

        float sb_white = 0;
        float sb_black = 0;

        double white_result = 0;
        double black_result = 0;
        String played = "0";


        for (Pairing p : round.pairings) {
            white = "\"" + p.getWhite().getfullname() + "\"";
            black = "\"" + p.getBlack().getfullname() + "\"";

            database.execSQL("INSERT INTO " + tournament.getName() + " VALUES ( " + totalRounds + " , " + round.roundNumber + 1 + ", " + white + "," + white_result + ", " + p.getWhite().getRating() + ", " +
                    p.getWhite().getPoints() + " , " + sb_white +
                    "," + black + "," + black_result + "," + p.getBlack().getRating() + ", " +
                    0 + " , " + sb_black + " , " + played + "  );");
        }

        if (players.size() % 2 == 1) {
            Player p = round.getBYE(players);
            System.out.println("am bye   *********************    " + p.getfullname());
            white = "\"" + p.getfullname() + "\"";
            black = "\"" + "BYE" + "\"";

            database.execSQL("INSERT INTO " + tournament.getName() + " VALUES (" + totalRounds + " , " + round.roundNumber + 1 + ", " + white + "," + 1 + "," + p.getRating() + ", " +
                    10 + " , " + sb_white + ", " + black + "," + 0 + "," + 0 + "," +
                    0 + " , " + 0 + ", 1 );");
        }

        database.close();
        return true;
    }

    public void insert_new_round(String tournamentName, Round round, PairingImpl byePair, int totalRounds, int roundNumber) {
        String white = "";
        String black = "";
        SQLiteDatabase database = this.getWritableDatabase();

        float sb_white = 0;
        float sb_black = 0;

        double white_result = 0;
        double black_result = 0;

        for (Pairing p : round.pairings) {
            white = "\"" + p.getWhite().getfullname() + "\"";
            black = "\"" + p.getBlack().getfullname() + "\"";
            String played = "0";


            sb_black = p.getBlack().getSonnebornBerner();
            sb_white = p.getWhite().getSonnebornBerner();

            database.execSQL("INSERT INTO " + tournamentName + " VALUES (" + totalRounds + " , " + roundNumber + ", " + white + "," + white_result + "," + p.getWhite().getRating() + ", " +
                    p.getWhite().getPoints() + " , " + sb_white +
                    "," + black + "," + black_result + "," + p.getBlack().getRating() + ", " +
                    p.getBlack().getPoints() + " , " + sb_black + " , " + played + ");");

        }

        if (byePair != null) {

            white = "\"" + byePair.getWhite().getfullname() + "\"";
            black = "\"" + byePair.getBlack().getfullname() + "\"";

            database.execSQL("INSERT INTO " + tournamentName + " VALUES (" + totalRounds + " , " + roundNumber + ", " + white + "," + 1 + "," +
                    byePair.getWhite().getRating() + ", " +
                    (byePair.getWhite().getPoints() + 10) + " , " + byePair.getWhite().getSonnebornBerner() +
                    "," + black + "," + 0 + "," + 0 + ", " +
                    0 + " , " + 0 + " , 1);");
        }
        database.close();
    }


    public void updateScore(PairingImpl p, String tournament, SQLiteDatabase database) {

        try {

            int score_w = p.getWhite().getPoints();
            int score_b = p.getBlack().getPoints();
            String played = "1";

            float sb_white = p.getWhite().getSonnebornBerner();
            float sb_black = p.getBlack().getSonnebornBerner();

            double white_result = 0;
            double black_result = 0;

            if (p.whiteWon()) white_result = 1;
            else if (p.isDraw()) {
                white_result = 0.5;
                black_result = 0.5;
            } else {
                black_result = 1;
            }

            database.execSQL("UPDATE " + tournament + " SET " + "white_result = " + white_result + " ," + " score_w = " + score_w + " , sb_white = " + sb_white
                    + ", black_result  = " + black_result + ", score_b =  " + score_b + " , sb_black = " + sb_black + " , played = " + played + " WHERE " + " white = " + "\"" +
                    p.getWhite().getfullname() + "\"" + " and " + " black = " + "\"" + p.getBlack().getfullname() + "\"" + " and played = 0;");


        } catch (SQLiteException e) {
            Log.e(LOGCAT, "Exception caught ", e);
        }
    }

    public void deleteTournament(String name) {
        Log.d(LOGCAT, "delete");
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "Drop table " + name;
        Log.d("query", deleteQuery);
        database.execSQL(deleteQuery);
        database.close();
    }

}

