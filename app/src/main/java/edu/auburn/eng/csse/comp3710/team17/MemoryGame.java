package edu.auburn.eng.csse.comp3710.team17;

import android.support.v4.app.FragmentActivity;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import android.os.Bundle;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.content.Context;
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
import android.view.Gravity;

import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;


public class MemoryGame extends FragmentActivity {
    boolean running = false;
    //static variables of row and comment length
    private static int ROW = -1;
    private static int COL = -1;

    private static Object locked = new Object();

    private UpdateHandler handler;
    //public List<Integer> cardIds = new ArrayList<Integer>();
    private List<Card> cardList = new ArrayList<>();
    
    //array list of card images
    List<Integer> cardBacks = new ArrayList<>();
    
    List<ImageButton> buttonList = new ArrayList<>();

    private Context context;
    private Drawable backPic;
    private ButtonListener buttonListener;
    private TableLayout gameBoard;

    private Card selection1;
    private Card selection2;

    //game running time objects
    private Timer gameTimer;
    private TimerTask timerTask;

    //game type
    private int mode;
    //attempted tries
    private int tries;
    //elapsed time
    private int totalTime;
    private int finalTime;
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
    private boolean difficultyChosen;
    //string identifiers for saving instance state
    static final String STATE_MODE = "mode";
    static final String STATE_SCORE = "score";
    static final String STATE_DIFFICULTY_CHOSEN = "chosen";

    static final String STATE_CARDS = "cards";
    static final String STATE_ON_BOARD = "onBoard";
    static final String STATE_FACE_UP = "faceUp";
    static final String STATE_ROW = "row";
    static final String STATE_COLUMN = "column";


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_game);
        difficultyChosen = false;

        //to be used to distinguish what game mode is selected
        mode = getIntent().getIntExtra("mode", 0);
        
        //handler which updates card status and checks for match
        handler = new UpdateHandler();
        
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
                difficultyChosen = true;

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
        
        //no card has been selected
        selection1 = null;
        
        //shuffle arraylist of images
        Collections.shuffle(cardBacks, new Random(seed));

        //add view of new row to tableLayout based on number of rows
        for (int i =0; i < ROW; i++) {
            gameBoard.addView(addRow(i));
        }

        selection1 = null;

        //initialize attempts
        tries = 0;
        totalTime = 0;
        finalTime = 0;
        //check if timed or classic mode
        if (mode == 2) {
          startTimer();
        } else {
            //counter of total tries
            ((TextView) findViewById(R.id.textTries)).setText("Total Turns: " + tries);
        }
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
                    if (mode == 2) {
                        finalScore = finalTime;
                        stopTimer();
                    } else {
                        finalScore = tries;
                    }
                    gameOver = true;

                    Toast.makeText(context, "Congratulations! You win!", Toast.LENGTH_SHORT).show();

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
                }
            }
            else {
                //no match, turn cards back over, set selections to null
                selection1.faceDown();
                selection2.faceDown();
                selection1.setFaceDown();
                selection2.setFaceDown();
            }
            selection1 = null;
            selection2 = null;
        }



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
                selection1.setFaceUp();
                
                /* eliminated for more logical method of determing attempts
                tries++;
                ((TextView)findViewById(R.id.textTries)).setText("Total Turns: "+ tries);
                */
            } else {
                //second card selection turn over card and allow handler to determine match
                selection2 = cardList.get(id);
                tries++;
                selection2.turnOver();
                selection2.setFaceUp();

                if (mode == 1) {

                    //counter of total tries
                    ((TextView) findViewById(R.id.textTries)).setText("Total Turns: " + tries);
                }


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

    //Determines if score is low enough to be added to top scores
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



    /**
     * Start Game Timer, creates new android timer and timer task, to update each second
     * 
     * */
    public void startTimer(){
        gameTimer = new Timer();
        timerTask = new myTimerTask();
        gameTimer.schedule(timerTask, 0, 1000);
    }
    public void stopTimer() {
        gameTimer.cancel();
        totalTime = 0;

    }
    private class myTimerTask extends TimerTask{
        @Override
        public void run() {

            totalTime++;
            updateTimer.sendEmptyMessage(0);
        }
    }
    private Handler updateTimer = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            //super.handleMessage(msg);

            int seconds = totalTime % 60;
            ((TextView) findViewById(R.id.textTries)).setText(String.format("Total Time Elapsed: %02d", seconds));
            finalTime = seconds;
        }

    };

    /**
     * We need to save all information needed to restore a game back to its previous state here.
     * List of things that need to be saved:
     * - Mode *Easy*
     * - Current score (TRIES OR TIME BASED ON MODE) *Easy*
     * - The cards in the order they appear on the board, if the cards are still on the board,
     * and if there was one card face up on the board already... This is the challenging one.
     * - Has difficulty been selected yet? If not, then we don't restore anything.
     * @param savedInstanceState
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (difficultyChosen) {
            int totalCards = ROW * COL;
            int[] cardIDs = new int[totalCards];
            boolean[] cardOnBoard = new boolean[totalCards];
            boolean[] cardFaceUp = new boolean[totalCards];
            int tm = totalMatches;

            for (Card c : cardList) {
                int index = c.getButton().getId();
                boolean matched = c.getMatch();
                boolean faceUp = c.isFaceUp();
                cardIDs[index] = c.getId();
                cardOnBoard[index] = matched;
                cardFaceUp[index] = faceUp;
            }
            savedInstanceState.putIntArray(STATE_CARDS, cardIDs);
            savedInstanceState.putBooleanArray(STATE_ON_BOARD, cardOnBoard);
            savedInstanceState.putBooleanArray(STATE_FACE_UP, cardFaceUp);
            savedInstanceState.putInt(STATE_ROW, ROW);
            savedInstanceState.putInt(STATE_COLUMN, COL);
            savedInstanceState.putInt(STATE_MATCHES, tm);
            savedInstanceState.putBoolean(STATE_DIFFICULTY_CHOSEN, difficultyChosen);
            savedInstanceState.putInt(STATE_MODE, mode);
            if (mode == 1)
                savedInstanceState.putInt(STATE_SCORE, tries);
            else
                savedInstanceState.putInt(STATE_SCORE, totalTime);

        }

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.getBoolean(STATE_DIFFICULTY_CHOSEN)) {
            difficultyChosen = true;
            gameBoard.removeView(findViewById(R.id.TableRow01));
            mode = savedInstanceState.getInt(STATE_MODE);
            ROW = savedInstanceState.getInt(STATE_ROW);
            COL = savedInstanceState.getInt(STATE_COLUMN);
            int[] cardIDs = savedInstanceState.getIntArray(STATE_CARDS);
            boolean[] cardOnBoard = savedInstanceState.getBooleanArray(STATE_ON_BOARD);
            boolean[] cardFaceUp = savedInstanceState.getBooleanArray(STATE_FACE_UP);
            totalMatches = savedInstanceState.getInt(STATE_MATCHES);

            gameBoard = (TableLayout)findViewById(R.id.TableLayout01);
            context = gameBoard.getContext();
            gameBoard.removeView(findViewById(R.id.TableRow01));
            TableRow newRow = ((TableRow)findViewById(R.id.TableRow02));
            newRow.removeAllViews();
            gameBoard = new TableLayout(context);
            newRow.addView(gameBoard);
            selection1 = null;
            selection2 = null;

            for (int rows = 0; rows < ROW; rows++) {
                TableRow row = new TableRow(context);
                row.setHorizontalGravity(Gravity.CENTER);
                for (int cols = 0; cols < COL; cols++) {
                    int index = (rows * COL) + cols;
                    ImageButton button = new ImageButton(context);
                    button.setId(index);
                    button.setOnClickListener(buttonListener);
                    //adding to buttonlist possibly not needed?
                    buttonList.add(button);
                    row.addView(button);
                    //important, creates a card using this newly created button and passed index to be used as id
                    Card card1 = new Card(button, cardIDs[index],
                            getResources().getDrawable(cardIDs[index]));
                    if (cardOnBoard[index]) {
                        card1.setMatch();
                        card1.faceDown();
                    }
                    if (cardFaceUp[index]) {
                        selection1 = card1;
                        selection1.setFaceUp();
                        selection1.turnOver();
                    } else {
                        card1.faceDown();
                    }
                    cardList.add(card1);
                }
                gameBoard.addView(row);
            }

            //initialize attempts
            if (mode == 2) {
                totalTime = savedInstanceState.getInt(STATE_SCORE);
                startTimer();
            } else {
                //counter of total tries
                tries = savedInstanceState.getInt(STATE_SCORE);
                ((TextView) findViewById(R.id.textTries)).setText("Total Turns: " + tries);
            }

        }
        super.onRestoreInstanceState(savedInstanceState);
    }

}

