package com.quod.postprocess;

import org.json.simple.JSONObject;
import com.quod.Constants;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MappingUtility {

    /**
     * convert string to Date
     *
     * @param dateStr
     * @return
     */
    public static Date strToDate(String dateStr) {
        if (Objects.isNull(dateStr) || dateStr == null || dateStr == "null") {
            return null;
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {

            Date result = dateFormat.parse(dateStr);
            return result;
        } catch (ParseException ex) {
            Logger.getLogger(MappingUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * convert date duration
     *
     * @param date1
     * @param date2
     * @param timeUnit
     * @return
     */
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return Math.abs(timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS));
    }

    /**
     * Maping objct json to event model
     * @param jsonObject
     * @return
     */
    public static Event jsonToEvent(JSONObject jsonObject) {
        try {
            String eventId = jsonObject.get("id").toString();
            JSONObject actor = (JSONObject) jsonObject.get("actor");
            String actorId = actor.get("id").toString();
            String actorAcc = actor.get("login").toString();
            Boolean isPublic = (Boolean) jsonObject.get("public");
            String eventType = jsonObject.get("type").toString();
            Date createTime = MappingUtility.strToDate(jsonObject.get("created_at").toString());
            Date closeTime = null;
            Date updateTime = null;
            String evenStatus = null;

            if (Constants.ISSUE_EVENT_TYPE.equalsIgnoreCase(eventType)) {
                JSONObject payload = (JSONObject) jsonObject.get("payload");
                JSONObject issue = (JSONObject) payload.get("issue");
                evenStatus = issue.get("state").toString();
                createTime = MappingUtility.strToDate(issue.get("created_at").toString());
                if ("closed".equalsIgnoreCase(evenStatus)) {
                    closeTime = MappingUtility.strToDate(issue.get("closed_at").toString());
                }
            } else if (Constants.PULL_REQUEST_EVENT_TYPE.equalsIgnoreCase(eventType)) {

                JSONObject payload = (JSONObject) jsonObject.get("payload");
                JSONObject pullRequest = (JSONObject) payload.get("pull_request");
                evenStatus = pullRequest.get("state").toString();
                createTime = MappingUtility.strToDate(pullRequest.get("created_at").toString());
                if ("closed".equalsIgnoreCase(evenStatus)) {
                    closeTime = MappingUtility.strToDate(pullRequest.get("closed_at").toString());
                }
            }


            JSONObject repoObj = (JSONObject) jsonObject.get("repo");
            String repoName  = repoObj.get("name").toString();
            String repoId = repoObj.get("id").toString();

            Event even = new Event(eventId, actorAcc, actorId, isPublic,
                    eventType, evenStatus, repoName, repoId, createTime, updateTime, closeTime);

            return even;
        } catch (Exception ex) {
            ex.getStackTrace();
            return null;
        }
    }

    /**
     * mapping json meta data to repository model
     *
     * @param eventList meta data
     * @param repoId    repo id
     * @return
     */
    public static Repo jsonToRepository(List<Event> eventList, String repoId) {
        List<Event> otherEventList = new ArrayList<>();
        List<Event> issueEventList = new ArrayList<>();
        List<Event> pushEventList = new ArrayList<>();
        List<Event> pullEventList = new ArrayList<>();

        List<Date> projectTimeLines = new ArrayList<>();
        String name = null;

        for (Event event : eventList) {
            String eventType = event.getEvenType();
            Date createTime = event.getCreateTime();
            if (Constants.ISSUE_EVENT_TYPE.equalsIgnoreCase(eventType)) {
                issueEventList.add(event);
            } else if (Constants.PUSH_EVENT_TYPE.equalsIgnoreCase(eventType)) {
                pushEventList.add(event);
            } else if (Constants.PULL_REQUEST_EVENT_TYPE.equalsIgnoreCase(eventType)) {
                pullEventList.add(event);
            } else {
                otherEventList.add(event);
            }

            if (name == null){
                name = event.getRepoName();
            }
            projectTimeLines.add(createTime);
        }

        List<Date> projectTimeLinesSorted = projectTimeLines.stream()
                .sorted(Comparator.comparingLong(Date::getTime))
                .collect(Collectors.toList());
        Date repoStartTime = null;
        Date repoEndTime = null;

        if (projectTimeLinesSorted.size() > 0) {
            repoStartTime = projectTimeLinesSorted.get(0);
        }

        if (projectTimeLinesSorted.size() >= 2) {
            repoEndTime = projectTimeLinesSorted.get(projectTimeLinesSorted.size() - 1);
        }

        Repo repository = new Repo(repoId, name, pushEventList, issueEventList,
                pullEventList, otherEventList, repoStartTime, repoEndTime);


        return repository;
    }
}
