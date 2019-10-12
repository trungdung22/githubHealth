package com.quod;

import com.quod.postprocess.Event;
import com.quod.postprocess.MappingUtility;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

public class UrlParserCallable implements Callable<List<Event>> {
    private final String url;

    public UrlParserCallable(String url) {
        this.url = url;
    }

    @Override
    public List<Event> call(){
        InputStream inputStream = null;
        Reader reader = null;
        BufferedReader buffered = null;
        HttpURLConnection con = null;
        List<Event> events = new ArrayList<>();
        try {
            URL obj = new URL(url);

            con = (HttpURLConnection) obj.openConnection();
            // optional default is GET
            con.setRequestMethod("GET");
            //add request header
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
            inputStream = new GZIPInputStream(con.getInputStream());
            reader = new InputStreamReader(inputStream, "UTF-8");
            buffered = new BufferedReader(reader);
            String inputLine;
            // read data and put into structure set
            while ((inputLine = buffered.readLine()) != null) {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(inputLine.toString());
                Event event = MappingUtility.jsonToEvent(jsonObject);
                if (event != null){
                    events.add(event);
                }
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(UrlParserCallable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | ParseException ex) {
            Logger.getLogger(UrlParserCallable.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                inputStream.close();
                reader.close();
                buffered.close();
                con.disconnect();
            } catch (IOException ex) {
                Logger.getLogger(UrlParserCallable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return events;
    }
}
