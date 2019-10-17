package com.quod.downloader;

import com.quod.dao.Event;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * Single thread download service class
 */
public class SingleThreadDownloadService extends DownloadService {

    @Override
    public List<Event> processRequest(List<String> urls) {
        List<Event> events = new ArrayList<>();
        for (String url : urls) {
            HttpURLConnection conn = null;
            InputStream inputStream = null;
            Reader reader = null;
            BufferedReader buffered = null;
            try {
                conn = this.initHttpConn(url);

                inputStream = new GZIPInputStream(conn.getInputStream());
                reader = new InputStreamReader(inputStream, "UTF-8");
                buffered = new BufferedReader(reader);
                List<Event> eventList = parseBufferData(buffered);
                events.addAll(eventList);
            } catch (IOException | ParseException ex) {
                Logger.getLogger(SingleThreadDownloadService.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    inputStream.close();
                    reader.close();
                    buffered.close();
                    conn.disconnect();
                } catch (IOException ex) {
                    Logger.getLogger(SingleThreadDownloadService.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        return null;
    }
}
