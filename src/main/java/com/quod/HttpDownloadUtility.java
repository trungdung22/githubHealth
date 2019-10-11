package com.quod;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import com.quod.postprocess.Event;
import com.quod.postprocess.MappingUtility;
import org.apache.commons.text.StringSubstitutor;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * Class utility that downloads data from github URL.
 */
public class HttpDownloadUtility {
    private static final int BUFFER_SIZE = 4096;

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private static List<String> urlParse(String baseUrl, Integer fromDate, Integer toDate,
                                         Integer formHour, Integer toHour){
        List<Integer> hours = Stream.iterate(formHour, n -> n + 1)
                .limit(23)
                .collect(Collectors.toList());
        List<Integer> dates = Stream.iterate(fromDate, n -> n + 1)
                .limit(toDate + 1 - fromDate)
                .collect(Collectors.toList());

        List<String> urls = new ArrayList<>();

        int dateCount = 0;
        for (Integer date : dates) {
            dateCount+=1;
            for (Integer hour : hours) {
                if (dateCount > dates.size() - 1){
                    if (hour >= toHour){
                        break;
                    }
                }
                String dateStr = date.toString();
                if (date < 10){
                    dateStr = "0" + dateStr;
                }
                String hourStr = hour.toString();

                Map<String, String> values = new HashMap<>();
                values.put("date", dateStr);
                values.put("time", hourStr);

                String url = StringSubstitutor.replace(baseUrl, values, "{", "}");
                urls.add(url);
            }
        }

        return urls;
    }
    /**
     * Downloads a file from a URL
     *
     * @param fileURL HTTP URL of the file to be downloaded
     **/
    public static Map<String, List<Event>> downloadFile(String fileURL,
                                                        Integer fromDate, Integer toDate, Integer fromHour, Integer toHour)
            throws IOException, ParseException {

        List<String> urls = urlParse(fileURL, fromDate, toDate, fromHour, toHour);
        Map<String, List<Event>> metaMap = new HashMap<>();

        for (String url : urls){
            URL obj = new URL(url);

            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            // optional default is GET
            con.setRequestMethod("GET");
            //add request header
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
            InputStream inputStream = new GZIPInputStream(con.getInputStream());
            Reader reader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader buffered  = new BufferedReader(reader);
            StringBuilder rawMetaList = new StringBuilder();

            try {
                String inputLine;
                // read data and put into structure set
                while ((inputLine = buffered.readLine()) != null) {
                    rawMetaList.append(inputLine.toString());
                }

            } catch (FileNotFoundException ex) {
                Logger.getLogger(HttpDownloadUtility.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(HttpDownloadUtility.class.getName()).log(Level.SEVERE, null, ex);

            } finally {
                try {
                    inputStream.close();
                    reader.close();
                    buffered.close();
                    con.disconnect();
                } catch (IOException ex) {
                    Logger.getLogger(HttpDownloadUtility.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            JSONParser parser = new JSONParser();
            JSONArray arrJson = (JSONArray) parser.parse("[" + rawMetaList.toString() + "]");
            for (Object json : arrJson){
                JSONObject jsonObject = (JSONObject) json;
                Event event = MappingUtility.jsonToEvent(jsonObject);
                if (event != null){
                    JSONObject jsonRepo = (JSONObject) jsonObject.get("repo");
                    String repoId = jsonRepo.get("id").toString();
                    List<Event> eventList = new ArrayList<>();

                    if (metaMap.containsKey(repoId)){
                        eventList = metaMap.get(repoId);
                    }
                    eventList.add(event);
                    metaMap.put(repoId, eventList);
                }
            }
        }

        //Read JSON response and print
        return  metaMap;
    }

}
