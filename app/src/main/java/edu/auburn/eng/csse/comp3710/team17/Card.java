package edu.auburn.eng.csse.comp3710.team17;

import android.graphics.drawable.Drawable;
import android.widget.ImageButton;
import android.view.View;

public class Card {

    //button of card displays image
    public ImageButton button;
    //whether card has been matched in game
    private boolean match;
    //id used to determine if cards are identical
    private int id;
    //back image received when creating card
    private Drawable back;

    // These two fields are used when restoring instance state to see if the card was face up/down
    // when restored or if the card had already been matched.
    private boolean faceUp;


    //creating a card takes parameters of the card's button, its image, and the drawableId which will be the card's Id as well.
    public Card(ImageButton button, int drawId, Drawable backImage) {

        match = false;
        this.id = drawId;

        back = backImage;
        this.button = button;
        this.button.setVisibility(View.VISIBLE);
        this.faceUp = false;

        this.faceDown();

    }
    /**
     * getter of button
     * returns card's button
     * */
    public ImageButton getButton() {
        return this.button;
    }
    /**
     * turns card face down
     * if a match, make invisible, else change image to back
     * */
    public void faceDown() {
        setFaceDown();
        if (!this.match) {
            this.button.setBackgroundResource(R.drawable.auback);
        } else {
            this.button.setBackgroundResource(R.drawable.auback);
            this.button.setVisibility(View.INVISIBLE);
        }
        this.button.setEnabled(true);
    }
    /**
     * determines if two cards are identical
     * returns true or false
     * */
    public boolean equals(Card card2) {
        return this.id == card2.getId();
    }

    /**
     * turn card over to reveal the image
     * 
     * */
    public void turnOver() {
        
        if (android.os.Build.VERSION.SDK_INT >= 16) {
            this.button.setBackground(back);
         }else {
            this.button.setBackgroundDrawable(back);
         }
        this.button.setEnabled(false);
    }

    /**
     * getter of card id
     * returns card's id
     * */
    public int getId() {
        return id;
    }
    /**
     * setter of boolean status matched
     * 
     * */
    public void setMatch() {
        this.match = true;
    }

    public void setFaceUp() {this.faceUp = true;}

    public void setFaceDown() {this.faceUp = false;}

    public boolean isFaceUp() {return faceUp;}

    public boolean getMatch() {return this.match;}

}
