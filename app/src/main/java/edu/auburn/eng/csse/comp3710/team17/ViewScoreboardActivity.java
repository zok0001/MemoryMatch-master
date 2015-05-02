package edu.auburn.eng.csse.comp3710.team17;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Zack on 4/30/2015.
 */
public class ViewScoreboardActivity extends FragmentActivity {
    final static String MODE = "mode"; // Game mode being played 1 = Classic 2 = Timed
    final static String DIFFICULTY = "difficulty"; // Difficulty being played on
    private ArrayList<TopScore> topScores = new ArrayList<>();
    private int numTopScores = 0;
    private String filename;
    private int difficulty;
    private int mode;
    private File hiScoreFile;
    private Button mainMenuButton;
    private TextView viewingScores;
    private TextView topScoreboardView;
    private Spinner scoreSpinner;
    private String topViewString;

    public void readScores() {
        String scoreboard = "";
        if (hiScoreFile.exists()) {
            Scanner scoreScan = null;
            try {
                scoreScan = new Scanner(hiScoreFile);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (scoreScan != null) {
                int first = 0;
                boolean empty = false;

                while (scoreScan.hasNextLine()) {
                    String line = scoreScan.nextLine();
                    if ((first == 0) && (line.equals("")) && !empty) {
                        scoreboard = "No scores yet!\nBe the first!";
                        empty = true;
                    } else {
                        numTopScores++;
                        Scanner lineScan = new Scanner(line);
                        String name = lineScan.next();
                        int score = lineScan.nextInt();
                        TopScore nextScore = new TopScore(name, score);
                        topScores.add(nextScore);
                    }
                    first++;
                }
                if (!empty) {
                    if (mode == 1) scoreboard = "NAME\t\tTRIES\n";
                    else scoreboard = "NAME\t\tTIME (S)\n";

                    for (int i = 0; i < numTopScores; i++) {
                        TopScore curScore = topScores.get(i);
                        scoreboard += (i + 1) + ") " + curScore.getName() + "\t\t" + curScore.getScore() + "\n";
                    }
                }
                if (topScores.isEmpty()) {
                    scoreboard = "No scores yet!\nBe the first!";
                }

            }
        }
        if (topScores.isEmpty()) {
            scoreboard = "No scores yet!\nBe the first!";
        }
        viewingScores.setText(scoreboard);

    }

    public void getFile() {
        switch(difficulty) {
            case(0): // Easy
                if (mode == 1) filename = "classic-easy.txt";
                else filename = "timed-easy.txt";
                break;
            case(1): // Medium
                if (mode == 1) filename = "classic-medium.txt";
                else filename = "timed-medium.txt";
                break;
            case(2): // Hard
                if (mode == 1) filename = "classic-hard.txt";
                else filename = "timed-hard.txt";
                break;
        }
        hiScoreFile = getFileStreamPath(filename);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scoreboard_view_layout);
    }

    @Override
    public void onStart() {
        super.onStart();

        mainMenuButton = (Button) findViewById(R.id.back_to_main_menu_button);
        topScoreboardView = (TextView) findViewById(R.id.top_scoreboard_view);
        viewingScores = (TextView) findViewById(R.id.viewing_scores);
        scoreSpinner = (Spinner) findViewById((R.id.scoreSpinner));


        mainMenuButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewScoreboardActivity.this, MenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.scoreboards,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scoreSpinner.setAdapter(adapter);
        scoreSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> a, View v, int position, long r) {
                ((Spinner) findViewById(R.id.scoreSpinner)).setSelection(0);

                switch(position) {
                    case 1:
                        mode = 1;
                        difficulty = 0;
                        break;
                    case 2:
                        mode = 1;
                        difficulty = 1;
                        break;
                    case 3:
                        mode = 1;
                        difficulty = 2;
                        break;
                    case 4:
                        mode = 2;
                        difficulty = 0;
                        break;
                    case 5:
                        mode = 2;
                        difficulty = 1;
                        break;
                    case 6:
                        mode = 2;
                        difficulty = 2;
                        break;
                    default:
                        return;
                }
                numTopScores = 0;
                topScores = new ArrayList<>();
                viewingScores.setText("");
                getFile();
                setTopTextView();
                readScores();
            }

            @Override
            public void onNothingSelected(AdapterView<?> a) {
                //default
            }


        });
    }

    public void setTopTextView() {
        // Set top TextView to say which mode and difficulty scoreboard is being viewed
        String modeString;
        String difficultyString = "";
        if (mode == 1) modeString = "Classic";
        else modeString = "Timed";
        switch(difficulty) {
            case(0):
                difficultyString = "Easy";
                break;
            case(1):
                difficultyString = "Normal";
                break;
            case(2):
                difficultyString = "Hard";
                break;
            default:
                break;
        }
        topScoreboardView.setText("Mode: " + modeString + "\nDifficulty: " + difficultyString);
    }
}
