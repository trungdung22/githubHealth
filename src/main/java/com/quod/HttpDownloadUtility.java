package com.quod;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import com.quod.postprocess.Event;
import com.quod.postprocess.MappingUtility;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.text.StringSubstitutor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * Class utility that downloads data from github URL.
 */
public class HttpDownloadUtility {
    private static final int BUFFER_SIZE = 4096;
    private static final int MYTHREADS = 10;

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    /**
     * url parer function
     *
     * @param baseUrl  base url
     * @param fromDate from date lim
     * @param toDate   to date lim
     * @param formHour hour lim from
     * @param toHour   hour lim to
     * @return list of string urls
     */
    private static List<String> urlParse(String baseUrl, Integer fromDate, Integer toDate,
                                         Integer formHour, Integer toHour) {
        List<Integer> hours = Stream.iterate(formHour, n -> n + 1)
                .limit(23)
                .collect(Collectors.toList());
        List<Integer> dates = Stream.iterate(fromDate, n -> n + 1)
                .limit(toDate + 1 - fromDate)
                .collect(Collectors.toList());

        List<String> urls = new ArrayList<>();

        int dateCount = 0;
        for (Integer date : dates) {
            dateCount += 1;
            for (Integer hour : hours) {
                if (dateCount > dates.size() - 1) {
                    if (hour > toHour) {
                        break;
                    }
                }
                String dateStr = date.toString();
                if (date < 10) {
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

    public static List<Event> processRequest(List<String> urls) {

        InputStream inputStream = null;
        Reader reader = null;
        BufferedReader buffered = null;
        HttpURLConnection con = null;

        List<Event> events = new ArrayList<>();

        for (String url : urls) {
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
        }

        return events;
    }

    public static List<Event> processRequestConcurrent(List<String> urls) {
        List<Future<List<Event>>> futures = new ArrayList<>();
        List<Event> eventList = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(MYTHREADS);

        for (int i = 0; i < urls.size(); i++) {

            String url = urls.get(i);
            Callable<List<Event>> worker = new UrlParserCallable(url);
            futures.add(executor.submit(worker));
        }
        executor.shutdown();
        // Wait until all threads are finish
        while (!executor.isTerminated()) {
        }

        try {
            for (Future<List<Event>> futureEvents : futures) {
                List<Event> events = futureEvents.get();
                eventList.addAll(events);
            }
        } catch (Exception ex) {
            Logger.getLogger(HttpDownloadUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return eventList;
    }

    /**
     * Downloads a file from a URL
     *
     * @param fileURL HTTP URL of the file to be downloaded
     **/
    public static Map<String, List<Event>> downloadFile(String fileURL,
                                                        Integer fromDate, Integer toDate, Integer fromHour, Integer toHour, Integer option){

        List<String> urls = urlParse(fileURL, fromDate, toDate, fromHour, toHour);
        Map<String, List<Event>> metaMap = new HashMap<>();
        List<Event> eventList;
        long startTime = System.currentTimeMillis();

        if (option == Constants.MULTITHREAD_DOWNLOAD_OPTIONS){
            System.out.println("-------------Download data by multithread---------------");
            eventList = processRequestConcurrent(urls);
        } else {
            System.out.println("--------------Download data by singlethread------------------");
            eventList = processRequest(urls);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Download data took " + ((endTime - startTime)/1000) + " seconds");

        if (!CollectionUtils.isEmpty(eventList)){
           try {
               for (Event event : eventList){
                   String repoId = event.getRepoId();
                   List<Event> eventSubList = new ArrayList<>();

                   if (metaMap.containsKey(repoId)){
                       eventSubList = metaMap.get(repoId);
                   }
                   eventSubList.add(event);
                   metaMap.put(repoId, eventSubList);
               }
           } catch (NullPointerException ex){
               Logger.getLogger(UrlParserCallable.class.getName()).log(Level.SEVERE, null, ex);
           }

        }
        //Read JSON response and print
        return metaMap;
    }

}
