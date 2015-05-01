package edu.auburn.eng.csse.comp3710.team17;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import android.view.Menu;
import android.os.Bundle;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.TableLayout;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TableRow;
import android.view.View;
import android.view.Gravity;

import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;


public class MemoryGame extends FragmentActivity {
    //static variables of row and comment length
    private static int ROW = -1;
    private static int COL = -1;

    private static Object locked = new Object();

    private UpdateHandler handler;
    //public List<Integer> cardIds = new ArrayList<Integer>();
    private List<Card> cardList = new ArrayList<Card>();
    
    //array list of card images
    List<Integer> cardBacks = new ArrayList<Integer>();
    
    List<ImageButton> buttonList = new ArrayList<ImageButton>();

    private Context context;
    private Drawable backPic;
    private ButtonListener buttonListener;
    private TableLayout gameBoard;

    private Card selection1;
    private Card selection2;

    //game type
    private int mode;
    //attempted tries
    private int tries;
    //# of matched cards
    private int totalMatches;
    long seed = System.nanoTime();

    // Keeps track of which difficulty is currently being played
    // 0 = easy, 1 = medium, 2 = hard
    private int curDifficulty;

    // The name of the file that contains the high scores for the particular mode and difficulty
    private String filename;
    private File hiScoreFile;
    private int finalScore;
    boolean gameOver;
    private TextView winningText;
    private Button submitButton;
    private EditText editInitials;




    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_game);

        //to be used to distinguish what game mode is selected
        mode = getIntent().getIntExtra("mode", 0);
        //mode = getIntent().getStringExtra("mode", 0);     alternative method of passing intent
        
        //handler which updates card status and checks for match
        handler = new UpdateHandler();

        //setContentView(R.layout.activity_memory_game);
        
        // setcontentview again?
        
        backPic = getResources().getDrawable(R.drawable.auback);

        // new buttonlistener for when a card is selected
        buttonListener = new ButtonListener();

        gameBoard = (TableLayout)findViewById(R.id.TableLayout01);

        context = gameBoard.getContext();

        winningText = (TextView) findViewById(R.id.winningText);
        submitButton = (Button)findViewById(R.id.submitButton);
        editInitials = (EditText)findViewById(R.id.editInitials);
        winningText.setVisibility(View.INVISIBLE);
        submitButton.setVisibility(View.INVISIBLE);
        editInitials.setVisibility(View.INVISIBLE);


        //Spinner selection for difficulty determines what size game board
        Spinner spinner = (Spinner) findViewById(R.id.sizeSpinner);
            ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.difficulty, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
              @Override
              public void onItemSelected(AdapterView<?> a, View v, int position, long r) {
                 ((Spinner) findViewById(R.id.sizeSpinner)).setSelection(0);

                  int x, y;
                  switch(position) {
                    case 1:
                        x= 3;
                        y= 4;
                        curDifficulty = 0;
                        break;
                    case 2:
                        x= 4;
                        y= 4;
                        curDifficulty = 1;
                        break;
                    case 3:
                        x= 4;
                        y= 5;
                        curDifficulty = 2;
                        break;
                    default:
                        return;
                }
                loadPictures(position);
                initialize( x, y, mode);
                gameOver = false;
              }

            @Override
            public void onNothingSelected(AdapterView<?> a) {
                //default
            }


        });

        submitButton.setOnClickListener(new OnClickListener() {
            private int numTopScores;
            private ArrayList<TopScore> topScores = new ArrayList<>();
            @Override
            public void onClick(View v) {
                if (canAddScore()) {
                    String initials = editInitials.getText().toString().trim();
                    if (initials.length() != 3 || containsWhiteSpace(initials)) {
                        Toast.makeText(context, "Initials must be 3 characters with no whitespace.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        int index;
                        getFile();
                        readScores();
                        // Check to see where new score should be inserted in list.
                        TopScore newScore = new TopScore(initials, finalScore);
                        for (index = 0; index < topScores.size(); index++) {
                            if (newScore.getScore() < topScores.get(index).getScore())
                                break;
                        }
                        if (numTopScores < 10) numTopScores++;
                        // Add new score at appropriate index, open correct score file, write scores to
                        // file
                        topScores.add(index, newScore);
                        addScore();

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(MemoryGame.this, MenuActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        }, 1400);
                    }
                }
                else {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(MemoryGame.this, MenuActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                    }, 1400);
                }
            }

            public void readScores() {
                Scanner scoreScan = null;
                try {
                    scoreScan = new Scanner(hiScoreFile);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                if (scoreScan != null) {
                    if (!scoreScan.hasNextLine()) {
                        numTopScores++;
                    }
                    else {
                        while (scoreScan.hasNextLine()) {
                            numTopScores++;
                            String line = scoreScan.nextLine();
                            Scanner lineScan = new Scanner(line);
                            String name = lineScan.next();
                            int score = lineScan.nextInt();
                            TopScore nextScore = new TopScore(name, score);
                            topScores.add(nextScore);
                        }
                    }
                }
            }

            public void getFile() {
                switch(curDifficulty) {
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

            public void addScore() {
                FileOutputStream fos;
                try {
                    fos = openFileOutput(hiScoreFile.getName(), Context.MODE_PRIVATE);
                    if (numTopScores == 1) {
                        TopScore curScore = topScores.get(0);
                        String scoreEntry = curScore.getName() + " " + curScore.getScore();
                        fos.write(scoreEntry.getBytes());
                    }
                    else {
                        for (int i = 0; i < numTopScores; i++) {
                            TopScore curScore = topScores.get(i);
                            if (i == 0) {
                                String scoreEntry= curScore.getName() + " " + curScore.getScore();
                                fos.write(scoreEntry.getBytes());
                            } else {
                                String scoreEntry = "\n" + curScore.getName() + " "
                                        + curScore.getScore();
                                fos.write(scoreEntry.getBytes());
                            }
                        }
                    }
                    fos.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }
    /**
     * handler maintains game and whether card's have been correctly matched
     * 
     * */
    class UpdateHandler extends Handler {

        @Override
        public void handleMessage(Message message) {
            synchronized (locked) {
                checkMatch();
            }
        }
        public void checkMatch () {
            if(selection1.equals(selection2)) {
                
                //set both cards as being matched
                selection1.setMatch();
                selection2.setMatch();
                
                //call card method to turn face down which will make invisible when matched
                selection1.faceDown();
                selection2.faceDown();
                totalMatches++;
                
                //check to see if game completed
                if (totalMatches == ((ROW*COL)/2)) {
                    finalScore = tries;
                    gameOver = true;
                    // TODO: END OF GAME SEQUENCE
                    // CHECK TO SEE IF THE FINAL SCORE OF THE CURRENT PLAY IS GOOD ENOUGH TO BE
                    // ADDED TO THE SCOREBOARD FOR THAT MODE AND DIFFICULTY. IF IT IS, LAUNCH THE
                    // FRAGMENT TO ADD THE SCORE. IF IT IS NOT, LAUNCH THE FRAGMENT TO SIMPLY VIEW
                    // THE HIGH SCORES.
                    // if (canAddScore()) {
                    //     //LAUNCH ScoreboardAddFragment
                    // }
                    // else {
                        //LAUNCH ScoreboardViewFragment
                    //}
                    Toast.makeText(context, "Congratulations! You win!", Toast.LENGTH_SHORT).show();

                    /** UNCOMMENT THIS SECTION TO SEE THE OTHER WAY */

                    String modeString = (mode == 1) ? "Classic" : "Timed";
                    String difficultyString;
                    if (curDifficulty == 0) difficultyString = "Easy";
                    else if (curDifficulty == 1) difficultyString = "Normal";
                    else difficultyString = "Hard";
                    String scoreType = (mode == 1) ? "moves" : "seconds";
                    String youWin = "Congratulations! You beat " + modeString + "\nmode on difficulty "
                            + difficultyString + " in " + finalScore + " " + scoreType;
                    youWin += "\nEnter your initials to submit your score! (3 Characters)";
                    winningText.setText(youWin);
                    submitButton.setText("SUBMIT");
                    winningText.setVisibility(View.VISIBLE);
                    submitButton.setVisibility(View.VISIBLE);
                    editInitials.setVisibility(View.VISIBLE);



                    /** THIS IS THE FIRST WAY I TRIED
                    // Reset the game after a short delay
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(MemoryGame.this, MenuActivity.class);
                            intent.putExtra("mode", mode);
                            intent.putExtra("score", finalScore);
                            intent.putExtra("difficulty", curDifficulty);
                            intent.putExtra("gameOver", true);
                            startActivity(intent);
                            finish();
                       }
                    }, 1400); */
                }
            }
            else {
                //no match, turn cards back over, set selections to null
                selection1.faceDown();
                selection2.faceDown();
            }
            selection1 = null;
            selection2 = null;
        }



    }
    /**
     * initialize settings based on spinner selection
     * 
     * */
    private void loadPictures(int case1) {

        switch(case1) {
            case 1:
                cardBacks.add(R.drawable.gus);
                cardBacks.add(R.drawable.nova);
                cardBacks.add(R.drawable.sam);
                cardBacks.add(R.drawable.stadium);
                cardBacks.add(R.drawable.aubie);
                cardBacks.add(R.drawable.bo);

                cardBacks.add(R.drawable.gus);
                cardBacks.add(R.drawable.nova);
                cardBacks.add(R.drawable.sam);
                cardBacks.add(R.drawable.stadium);
                cardBacks.add(R.drawable.aubie);
                cardBacks.add(R.drawable.bo);

                break;
            case 2:
                cardBacks.add(R.drawable.gus);
                cardBacks.add(R.drawable.nova);
                cardBacks.add(R.drawable.sam);
                cardBacks.add(R.drawable.stadium);
                cardBacks.add(R.drawable.aubie);
                cardBacks.add(R.drawable.bo);
                cardBacks.add(R.drawable.bruce);
                cardBacks.add(R.drawable.cam);

                cardBacks.add(R.drawable.gus);
                cardBacks.add(R.drawable.nova);
                cardBacks.add(R.drawable.sam);
                cardBacks.add(R.drawable.stadium);
                cardBacks.add(R.drawable.aubie);
                cardBacks.add(R.drawable.bo);
                cardBacks.add(R.drawable.bruce);
                cardBacks.add(R.drawable.cam);

                break;
            case 3:
                cardBacks.add(R.drawable.gus);
                cardBacks.add(R.drawable.nova);
                cardBacks.add(R.drawable.sam);
                cardBacks.add(R.drawable.stadium);
                cardBacks.add(R.drawable.aubie);
                cardBacks.add(R.drawable.bo);
                cardBacks.add(R.drawable.bruce);
                cardBacks.add(R.drawable.cam);
                cardBacks.add(R.drawable.logo);
                cardBacks.add(R.drawable.charles);

                cardBacks.add(R.drawable.gus);
                cardBacks.add(R.drawable.nova);
                cardBacks.add(R.drawable.sam);
                cardBacks.add(R.drawable.stadium);
                cardBacks.add(R.drawable.aubie);
                cardBacks.add(R.drawable.bo);
                cardBacks.add(R.drawable.bruce);
                cardBacks.add(R.drawable.cam);
                cardBacks.add(R.drawable.logo);
                cardBacks.add(R.drawable.charles);

                break;
            default:
                return;
        }

    }



    /**
     * initialize game board
     * 
     * */
    private void initialize(int c, int r, int mode) {
        ROW = r;
        COL = c;

        //remove TableRow containing spinner
        gameBoard.removeView(findViewById(R.id.TableRow01));

        //Add Table row which will contain array and columns of table layouts of views (Image Buttons)
        TableRow newRow = ((TableRow)findViewById(R.id.TableRow02));
        newRow.removeAllViews();

        gameBoard = new TableLayout(context);

        newRow.addView(gameBoard);

        //List of created cards
        cardList.clear();
        
        //no card has been selectd
        selection1 = null;
        
        //shuffle arraylist of images
        Collections.shuffle(cardBacks, new Random(seed));

        //add view of new row to tablelayout based on number of rows
        for (int i =0; i < ROW; i++) {
            gameBoard.addView(addRow(i));
        }

        selection1 = null;

        //intialize attempts
        tries = 0;
        //counter of total tries
        ((TextView)findViewById(R.id.textTries)).setText("Total Turns: "+ tries);
    }
    /**
     * adds a row
     * returns TableRow
     * */
    private TableRow addRow(int x) {
        TableRow row = new TableRow(context);

        row.setHorizontalGravity(Gravity.CENTER);
        
        // create button and add to row based on column index
        for (int i = 0; i < COL; i++) {
            ImageButton button = new ImageButton(context);
            //index will match card id, for now. going to change randomization
            int index = x * COL + i;
            
            button.setId(index);
            button.setOnClickListener(buttonListener);
            //adding to buttonlist possibly not needed?
            buttonList.add(button);
            row.addView(button);
            //important, creates a card using this newly created button and passed index to be used as id
            createCard(button, index);
        }
        return row;
    }
    public void createCard(ImageButton button, int index) {
        int drawId = cardBacks.get(index);
        //create card with button, drawable, and id as parameters
        Card card1 = new Card(button, drawId, getResources().getDrawable(drawId));
        // the index of this card in the cardlist will correspond to the id assigned to its button.
        cardList.add(card1);
    }
    
    /**
     * Listener takes action when button pressed
     * 
     * */
    class ButtonListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            synchronized(locked) {
                if(selection1!=null && selection2!=null){
                    return;
                }
                int id = v.getId();
                turnFaceUp((ImageButton) v, id);



            }
        }

        private void turnFaceUp(ImageButton button, int id) {
            //first button pressed, button id matches card id, turnover card to see image
            if(selection1 == null) {
                selection1 = cardList.get(id);
                selection1.turnOver();
                
                /* eliminated for more logical method of determing attempts
                tries++;
                ((TextView)findViewById(R.id.textTries)).setText("Total Turns: "+ tries);
                */
            } else {
                //second card selection turn over card and allow handler to determine match
                selection2 = cardList.get(id);
                tries++;
                selection2.turnOver();

                ((TextView)findViewById(R.id.textTries)).setText("Total Turns: "+ tries);
                TimerTask task = new TimerTask() {
                
                //ensures user has ample time to see both card images before game determines whether they match
                    @Override
                    public void run() {
                        try {
                            synchronized (locked) {
                                handler.sendEmptyMessage(0);
                            }
                        } catch (Exception e) {
                            Log.e("E1", e.getMessage());
                        }
                    }
                };

                Timer timer = new Timer(false);
                timer.schedule(task, 1300);
            }
        }



    }

    public boolean canAddScore() {
        int numTopScores = 0;
        ArrayList<TopScore> topScores = new ArrayList<>();
        boolean canAdd = false;
        // Find the correct Top Score file
        switch (curDifficulty) {
            case (0): // Easy
                if (mode == 1) filename = "classic-easy.txt";
                else filename = "timed-easy.txt";
                break;
            case (1): // Medium
                if (mode == 1) filename = "classic-medium.txt";
                else filename = "timed-medium.txt";
                break;
            case (2): // Hard
                if (mode == 1) filename = "classic-hard.txt";
                else filename = "timed-hard.txt";
                break;
        }
        // File filesDir = getFilesDir();
        hiScoreFile = getFileStreamPath(filename);

        if (!hiScoreFile.exists()) {
            try {
                hiScoreFile.createNewFile();
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Scan the file and get all the current top scores
        Scanner scoreScan = null;
        try {
            scoreScan = new Scanner(hiScoreFile);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (scoreScan != null) {
            while (scoreScan.hasNextLine()) {
                numTopScores++;
                String line = scoreScan.nextLine();
                Scanner lineScan = new Scanner(line);
                String name = lineScan.next();
                int score = lineScan.nextInt();
                TopScore nextScore = new TopScore(name, score);
                topScores.add(nextScore);
            }
        }

        // Check to see if there are less than 10 score or if the current score is better than the
        // last place score currently on the score board.
        if (numTopScores < 10) canAdd = true; // Scoreboard isn't full
        else { // Scoreboard is full with 10 entries
            TopScore lastScore = topScores.get(9);
            if (finalScore < lastScore.getScore()) canAdd = true;
        }

        return canAdd;
    }

    public static boolean containsWhiteSpace(final String testCode){
        if(testCode != null){
            for(int i = 0; i < testCode.length(); i++){
                if(Character.isWhitespace(testCode.charAt(i))){
                    return true;
                }
            }
        }
        return false;
    }

}
