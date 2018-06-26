/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

/**
 *
 * @author MikaF
 */
public class LeaderboardEntry {
    
    private String name;
    private int score;

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public LeaderboardEntry(String name, int score) {
        this.name = name;
        this.score = score;
    }

    @Override
    public String toString() {
        return name + " : " + score;
    }
    
}
