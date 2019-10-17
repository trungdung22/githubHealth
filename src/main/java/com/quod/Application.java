package com.quod;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.quod.dao.FileWriterUtils;
import com.quod.downloader.HttpDownloadUtility;
import com.quod.dao.Event;
import com.quod.postprocess.HealthMetric;
import com.quod.mappingUtils.MappingUtility;
import com.quod.dao.Repo;
import org.apache.commons.collections4.CollectionUtils;

/**
 * Main Class application
 */
public class Application {
    // Method to encode a string value using `UTF-8` encoding scheme
    private static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    private static String decodeValue(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    private static Map<String, Integer> parseCMDParam(String paramStr) {
        Map<String, Integer> map = new HashMap<>();

        String dateStr = paramStr.split("T")[0].split("-")[2];
        String hourStr = paramStr.split("T")[1].split(":")[0];

        map.put("date", Integer.parseInt(dateStr));
        map.put("hour", Integer.parseInt(hourStr));
        return map;
    }

    /**
     * list event to hashmap
     *
     * @param eventList list of repo event data
     * @return hashmap meta data
     */
    private static Map<String, List<Event>> map(List<Event> eventList) {
        Map<String, List<Event>> metaMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(eventList)) {
            try {
                for (Event event : eventList) {
                    String repoId = event.getRepoId();
                    List<Event> eventSubList = new ArrayList<>();

                    if (metaMap.containsKey(repoId)) {
                        eventSubList = metaMap.get(repoId);
                    }
                    eventSubList.add(event);
                    metaMap.put(repoId, eventSubList);
                }
            } catch (NullPointerException ex) {
                Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return metaMap;
    }

    private static List<Repo> mappingEventToRepo(List<Event> eventList){
        //Read JSON response and print
        Map<String, List<Event>> metaMap = map(eventList);

        List<Repo> repoList = new ArrayList<>();

        for (Map.Entry<String, List<Event>> entry : metaMap.entrySet()) {
            String repoId = entry.getKey();
            List<Event> jsonObjectList = entry.getValue();

            Repo repo = MappingUtility.jsonToRepository(jsonObjectList, repoId);
            repoList.add(repo);
        }
        return repoList;
    }

    public static void main(String args[]) {
        String firstArg = args[0];
        String seconArg = args[1];

        Map<String, Integer> fromMap = parseCMDParam(firstArg);
        Map<String, Integer> toMap = parseCMDParam(seconArg);
        String baseUrl = "https://data.gharchive.org/";
        String yearStr = firstArg.split("T")[0].split("-")[0];
        String monStr = firstArg.split("T")[0].split("-")[1];
        String url = yearStr + "-" + monStr  + "-{date}-{time}.json.gz";

        List<Event> eventList = HttpDownloadUtility.processData(baseUrl + decodeValue(url),
                fromMap.get("date"), toMap.get("date"), fromMap.get("hour"), toMap.get("hour"),
                Constants.MULTITHREAD_DOWNLOAD_OPTIONS);

        List<Repo> repoList = mappingEventToRepo(eventList);

        HealthMetric.computeMetric(repoList);

        List<Repo> sortedList = repoList.stream()
                .sorted(Comparator.comparingDouble(Repo::getTotalScore).reversed())
                .collect(Collectors.toList());
        FileWriterUtils.saveToCSV(sortedList);
    }
}
