package edu.auburn.eng.csse.comp3710.team17;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

public class MenuActivity extends FragmentActivity {
    private int mode;
    private int difficulty;
    private int score;
    private boolean gameOver;
    private Button viewScoresButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        /** final FragmentActivity thisActivity = this; */
        viewScoresButton = (Button) findViewById(R.id.scoreboardButton);
        /**
        viewScoresButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(thisActivity, ViewScoreboardActivity.class);
                startActivity(intent);
            }
        }); */

    }

    public void runClassicGame(View view) {
        Intent intent = new Intent(this, MemoryGame.class);
        intent.putExtra("mode", 1);
        startActivity(intent);
    }

    public void runTimedGame(View view) {
        Intent intent = new Intent(this, MemoryGame.class);
        intent.putExtra("mode", 2);
        startActivity(intent);
    }

    public void viewScore(View view) {
        Intent intent = new Intent(this, ViewScoreboardActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
