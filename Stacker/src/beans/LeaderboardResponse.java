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
public class LeaderboardResponse {
    
    private int responseCode;
    private LeaderboardEntry[] entries;

    public int getResponseCode() {
        return responseCode;
    }

    public LeaderboardEntry[] getEntries() {
        return entries;
    }

    public LeaderboardResponse(int responseCode, LeaderboardEntry[] entries) {
        this.responseCode = responseCode;
        this.entries = entries;
    }
    
}
