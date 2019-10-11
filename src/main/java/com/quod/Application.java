package com.quod;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import com.quod.postprocess.Event;
import com.quod.postprocess.HealthMetric;
import com.quod.postprocess.MappingUtility;
import com.quod.postprocess.Repo;
import org.json.simple.parser.ParseException;

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

    public static String decodeValue(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    private static Map<String, Integer> parseParam(String paramStr) {
        Map<String, Integer> map = new HashMap<>();

        String dateStr = paramStr.split("T")[0].split("-")[2];
        String hourStr = paramStr.split("T")[1].split(":")[0];

        map.put("date", Integer.parseInt(dateStr));
        map.put("hour", Integer.parseInt(hourStr));
        return map;
    }

    public static void main(String args[]) {
        String firstArg = args[0];
        String seconArg = args[1];

        Map<String, Integer> fromMap = parseParam(firstArg);
        Map<String, Integer> toMap = parseParam(seconArg);
        try {
            String baseUrl = "https://data.gharchive.org/";
            String yearStr = firstArg.split("T")[0].split("-")[0];
            String monStr = firstArg.split("T")[0].split("-")[1];
            String url = yearStr + "-" + monStr  + "-{date}-{time}.json.gz";

            Map<String, List<Event>> metaMap = HttpDownloadUtility.downloadFile(baseUrl + decodeValue(url),
                    fromMap.get("date"), toMap.get("date"), fromMap.get("hour"), toMap.get("hour"));
            List<Repo> repoList = new ArrayList<>();

            for (Map.Entry<String, List<Event>> entry : metaMap.entrySet()) {
                String repoId = entry.getKey();
                List<Event> jsonObjectList = entry.getValue();

                Repo repo = MappingUtility.jsonToRepository(jsonObjectList, repoId);
                repoList.add(repo);
            }

            HealthMetric.computeMetric(repoList);
            List<Repo> sortedList = repoList.stream()
                    .sorted(Comparator.comparingDouble(Repo::getTotalScore).reversed())
                    .collect(Collectors.toList());
            FileWriterUtils.saveToCSV(sortedList);
        } catch (IOException | ParseException ex) {
            ex.printStackTrace();
        }
    }
}
