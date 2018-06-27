/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net;

import beans.LeaderboardEntry;
import beans.LeaderboardResponse;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author MikaF
 */
public class Leaderboards {
    
    public static final String SERVER_DOMAIN = "stacker.game-server.cc";
    
    // getLeaderboards() Response Codes
    // 0  - OK Response
    // 10 - Invalid Domain Name
    // -1 - Server unreachable (Timeout)
    // -2 - Unexpected response format
    // Other: HTTP Response Codes
    
    public static LeaderboardResponse getLeaderboards() {
        LeaderboardEntry[] entries = new LeaderboardEntry[10];
        URL url = null;
        try {
            url = new URL("http://" + SERVER_DOMAIN + "/api/index.php");
        } catch (MalformedURLException ex) {
            return new LeaderboardResponse(10, entries);
        }
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            Map<String, String> params = new HashMap<>();
            params.put("action", "get_leaderboards");
            
            con.setDoOutput(true);
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(ParameterStringBuilder.getParamsString(params));
            out.flush();
            out.close();            
        } catch (IOException e) {
            return new LeaderboardResponse(-1, entries);
        }
        
        int status = -1;
        String content = "";
        try {
            status = con.getResponseCode();
            if(status != 200)
                return new LeaderboardResponse(status, entries);
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while((line = br.readLine()) != null) {
                content += line + "\n";
            }
            br.close();
            con.disconnect();
        } catch(IOException e) {
            return new LeaderboardResponse(status, entries);
        }
        if(!content.matches("(.+\\;\\d+\\n){10}"))
            return new LeaderboardResponse(-2, entries);
        String[] parts = content.split("\\n");
        for(int i = 0; i < 10; i++) {
            entries[i] = new LeaderboardEntry(parts[i].split(";")[0], Integer.parseInt(parts[i].split(";")[1]));
        }
        return new LeaderboardResponse(0, entries);
    }
    
    // addEntry() Response Codes
    // 0 - OK Response
    // 1 - Invalid Domain Name
    // 2 - Server Unreachable (Timeout)
    // 3 - Invalid Response
    // Other: HTTP Response Codes
    
    public static int addEntry(LeaderboardEntry entry) {
        URL url = null;
        try {
            url = new URL("http://" + SERVER_DOMAIN + "/api/index.php");
        } catch (MalformedURLException ex) {
            return 1;
        }
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            Map<String, String> params = new HashMap<>();
            params.put("action", "add_entry");
            params.put("name", entry.getName());
            params.put("score", entry.getScore()+"");
            
            con.setDoOutput(true);
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(ParameterStringBuilder.getParamsString(params));
            out.flush();
            out.close();            
        } catch (IOException e) {
            return 2;
        }
        
        int status = -1;
        String content = "";
        try {
            status = con.getResponseCode();
            if(status != 200)
                return status;
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while((line = br.readLine()) != null) {
                content += line + "\n";
            }
            br.close();
            con.disconnect();
        } catch(IOException e) {
            return 2;
        }
        System.out.println(content);
        if(!content.contains("Success"))
            return 3;
        return 0;
    }
    
    // resetLeaderboard() Response Codes
    // 0 - OK Response
    // 1 - Invalid Domain Name
    // 2 - Server Unreachable (Timeout)
    // 3 - Invalid Response
    // Other: HTTP Response Codes
    
    public static int resetLeaderboard() {
        URL url = null;
        try {
            url = new URL("http://" + SERVER_DOMAIN + "/api/index.php");
        } catch (MalformedURLException ex) {
            return 1;
        }
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            Map<String, String> params = new HashMap<>();
            params.put("action", "clear_leaderboard");
            
            con.setDoOutput(true);
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(ParameterStringBuilder.getParamsString(params));
            out.flush();
            out.close();            
        } catch (IOException e) {
            return 2;
        }
        
        int status = -1;
        String content = "";
        try {
            status = con.getResponseCode();
            if(status != 200)
                return status;
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while((line = br.readLine()) != null) {
                content += line + "\n";
            }
            br.close();
            con.disconnect();
        } catch(IOException e) {
            return 2;
        }
        if(!content.contains("Success"))
            return 3;
        return 0;
    }
    
}
