package edu.auburn.eng.csse.comp3710.team17;

/**
 * Data structure to store high scores with the names assoicated with them.
 *
 * Created by Zack on 4/28/2015.
 */
public class TopScore {
    private String name;
    private int score;

    public TopScore(String n, int s) {
        this.score = s;
        this.name = n;
    }

    public void setScore(int s) { this.score = s; }

    public void setName(String n) { this.name = n; }

    public int getScore() { return this.score; }

    public String getName() { return this.name; }

}
