package edu.auburn.eng.csse.comp3710.team17;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Zack on 5/1/2015.
 */
public class VersusMode extends FragmentActivity {

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

    //attempted tries
    private int pOneMatches;
    private int pTwoMatches;
    boolean p1Turn;
    //# of matched cards
    private int totalMatches;
    long seed = System.nanoTime();
    boolean gameOver;

    TextView pOneMatchView;
    TextView pTwoMatchView;

    static final String STATE_CARDS = "cards";
    static final String STATE_MATCHES = "matches";
    static final String STATE_ON_BOARD = "onBoard";
    static final String STATE_FACE_UP = "faceUp";
    static final String STATE_ROW = "row";
    static final String STATE_COLUMN = "column";
    static final String STATE_P1_SCORE = "pOneScore";
    static final String STATE_P2_SCORE = "pTwoScore";
    static final String STATE_WHOSE_TURN_IS_IT_ANYWAY = "turn";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.versus_mode_layout);

        //handler which updates card status and checks for match
        handler = new UpdateHandler();

        backPic = getResources().getDrawable(R.drawable.auback);

        // new buttonlistener for when a card is selected
        buttonListener = new ButtonListener();

        gameBoard = (TableLayout)findViewById(R.id.versusLayout);
        // Initialize game board
        context = gameBoard.getContext();
        if (savedInstanceState ==  null) {
            loadPictures();
            initialize();
        }
        gameOver = false;

        pOneMatches = 0;
        pTwoMatches = 0;
        totalMatches = 0;
        p1Turn = true;

        pOneMatchView = (TextView) findViewById(R.id.pOneMatches);
        pTwoMatchView = (TextView) findViewById(R.id.pTwoMatches);
        pOneMatchView.setText("Player 1 matches: " + pOneMatches);
        pTwoMatchView.setText("Player 2 matches: " + pTwoMatches);

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

                if (p1Turn) pOneMatches++;
                else pTwoMatches++;

                //check to see if game completed
                if (totalMatches == ((ROW*COL)/2)) {
                    gameOver = true;
                    String winner;
                    if (pOneMatches > pTwoMatches) winner = "Player number one wins!";
                    else if (pOneMatches < pTwoMatches) winner = "Player number two wins!";
                    else winner = "It's a tie!";
                    Toast.makeText(context, winner, Toast.LENGTH_SHORT).show();


                     // Reset the game after a short delay
                     handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(VersusMode.this, MenuActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                    }, 1400);
                }
            }
            else {
                //no match, turn cards back over, set selections to null
                selection1.faceDown();
                selection2.faceDown();
                selection1.setFaceDown();
                selection2.setFaceDown();
                if (p1Turn) {
                    Toast.makeText(context, "Player Two's turn", Toast.LENGTH_SHORT).show();
                    p1Turn = false;
                } else {
                    Toast.makeText(context, "Player One's turn", Toast.LENGTH_SHORT).show();
                    p1Turn = true;
                }
             }
            selection1 = null;
            selection2 = null;

            pOneMatchView.setText("Player 1 matches: " + pOneMatches);
            pTwoMatchView.setText("Player 2 matches: " + pTwoMatches);
        }



    }
    /**
     * initialize settings based on spinner selection
     *
     * */
    private void loadPictures() {
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
        cardBacks.add(R.drawable.toomers);
        cardBacks.add(R.drawable.aulogo);

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
        cardBacks.add(R.drawable.toomers);
        cardBacks.add(R.drawable.aulogo);

    }



    /**
     * initialize game board
     *
     * */
    private void initialize() {
        ROW = 6;
        COL = 4;

        //Add Table row which will contain array and columns of table layouts of views (Image Buttons)
        TableRow newRow = ((TableRow)findViewById(R.id.versusRow02));
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
    class ButtonListener implements View.OnClickListener {

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
            } else {
                //second card selection turn over card and allow handler to determine match
                selection2 = cardList.get(id);
                selection2.turnOver();
                selection2.setFaceUp();
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

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
        savedInstanceState.putInt(STATE_P1_SCORE, pOneMatches);
        savedInstanceState.putInt(STATE_P2_SCORE, pTwoMatches);
        savedInstanceState.putBoolean(STATE_WHOSE_TURN_IS_IT_ANYWAY, p1Turn);
        savedInstanceState.putInt(STATE_MATCHES, tm);


        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        gameBoard.removeView(findViewById(R.id.TableRow01));
        ROW = savedInstanceState.getInt(STATE_ROW);
        COL = savedInstanceState.getInt(STATE_COLUMN);
        int[] cardIDs = savedInstanceState.getIntArray(STATE_CARDS);
        boolean[] cardOnBoard = savedInstanceState.getBooleanArray(STATE_ON_BOARD);
        boolean[] cardFaceUp = savedInstanceState.getBooleanArray(STATE_FACE_UP);
        totalMatches = savedInstanceState.getInt(STATE_MATCHES);

        gameBoard = (TableLayout)findViewById(R.id.versusLayout);
        context = gameBoard.getContext();
        TableRow newRow = ((TableRow)findViewById(R.id.versusRow02));
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
        pOneMatches = savedInstanceState.getInt(STATE_P1_SCORE);
        pTwoMatches = savedInstanceState.getInt(STATE_P2_SCORE);
        p1Turn = savedInstanceState.getBoolean(STATE_WHOSE_TURN_IS_IT_ANYWAY);
        pOneMatchView.setText("Player 1 matches: " + pOneMatches);
        pTwoMatchView.setText("Player 2 matches: " + pTwoMatches);


        super.onRestoreInstanceState(savedInstanceState);
    }

}
