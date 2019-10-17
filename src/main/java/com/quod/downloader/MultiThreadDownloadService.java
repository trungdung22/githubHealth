package com.quod.downloader;

import com.quod.dao.Event;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * Multi thread download service class
 */
public class MultiThreadDownloadService extends DownloadService {

    private static final int THREADS_NUM = 10;

    @Override
    public List<Event> processRequest(List<String> urls) {
        List<Future<List<Event>>> futures = new ArrayList<>();
        List<Event> eventList = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(THREADS_NUM);

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
     * Mutipthread Class Callable Unit
     */
    public class UrlParserCallable implements Callable<List<Event>> {
        private final String url;

        public UrlParserCallable(String url) {
            this.url = url;
        }

        @Override
        public List<Event> call() {
            InputStream inputStream = null;
            Reader reader = null;
            BufferedReader buffered = null;
            HttpURLConnection con = null;
            List<Event> events = new ArrayList<>();
            try {
                con = initHttpConn(url);
                inputStream = new GZIPInputStream(con.getInputStream());
                reader = new InputStreamReader(inputStream, "UTF-8");
                buffered = new BufferedReader(reader);
                List<Event> eventList = parseBufferData(buffered);
                events.addAll(eventList);
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

}
