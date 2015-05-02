package edu.auburn.eng.csse.comp3710.team17;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class MenuActivity extends FragmentActivity
        implements HowToPlayFragment.OnMenuButtonClickedListener {
    private ImageView image;
    private LinearLayout buttonLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

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

    public void versusMode (View view) {
        Intent intent = new Intent(this, VersusMode.class);
        startActivity(intent);
    }

    public void showHowToPlay(View view) {
        buttonLayout = (LinearLayout) findViewById(R.id.buttonsLayout);
        image = (ImageView) findViewById(R.id.imageView);
        buttonLayout.setVisibility(View.INVISIBLE);
        image.setVisibility(View.INVISIBLE);
        HowToPlayFragment htpFrag = HowToPlayFragment.newInstance();
        htpFrag.setArguments(getIntent().getExtras());

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.menuActivity, htpFrag, "HOW TO PLAY");
        transaction.commit();
    }

    public void makeMenuVisible() {
        buttonLayout = (LinearLayout) findViewById(R.id.buttonsLayout);
        image = (ImageView) findViewById(R.id.imageView);
        buttonLayout.setVisibility(View.VISIBLE);
        image.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onMenuPressed() {
        makeMenuVisible();
    }

}
