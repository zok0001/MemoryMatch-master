package edu.auburn.eng.csse.comp3710.team17;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class SplashScreen extends Activity {

    private final int DISPLAY_TIME = 4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this, MenuActivity.class);
                SplashScreen.this.startActivity(intent);
                SplashScreen.this.finish();
            }

        }, DISPLAY_TIME);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

