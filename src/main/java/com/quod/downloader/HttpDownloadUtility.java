package com.quod.downloader;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.quod.dao.Event;
import org.apache.commons.text.StringSubstitutor;


/**
 * Class utility that downloads data from github URL.
 */
public class HttpDownloadUtility {
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
    private static List<String> urlParser(String baseUrl, Integer fromDate, Integer toDate,
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

    /**
     *
     * @param fileURL
     * @param fromDate
     * @param toDate
     * @param fromHour
     * @param toHour
     * @param option
     * @return
     */
    public static List<Event> processData(String fileURL,
                                          Integer fromDate, Integer toDate, Integer fromHour, Integer toHour, Integer option) {

        List<String> urls = urlParser(fileURL, fromDate, toDate, fromHour, toHour);
        DownloadService downloadServ = DownloadServiceProvider.getDownloadService(option);

        long startTime = System.currentTimeMillis();
        List<Event> eventList = downloadServ.processRequest(urls);
        long endTime = System.currentTimeMillis();
        System.out.println("Download data took " + ((endTime - startTime) / 1000) + " seconds");

        return eventList;
    }
}
