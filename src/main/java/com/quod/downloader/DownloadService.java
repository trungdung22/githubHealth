package com.quod.downloader;

import com.quod.dao.Event;
import com.quod.mappingUtils.MappingUtility;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * download service sbstract class
 */
public abstract class DownloadService {

    private static final int BUFFER_SIZE = 4096;

    public abstract List<Event> processRequest(List<String> urls);

    public HttpURLConnection initHttpConn(String url){
        try{
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            // optional default is GET
            con.setRequestMethod("GET");
            //add request header
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
            return con;
        } catch (IOException ex) {
            Logger.getLogger(DownloadService.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static List<Event> parseBufferData(BufferedReader buffered) throws IOException, ParseException {
        List<Event> events = new ArrayList<>();
        String inputLine;
        // read data and put into structure set
        while ((inputLine = buffered.readLine()) != null) {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(inputLine.toString());
            Event event = MappingUtility.jsonToEvent(jsonObject);
            if (event != null) {
                events.add(event);
            }
        }
        return events;
    }
}
