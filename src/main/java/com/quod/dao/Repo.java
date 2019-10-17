package com.quod.dao;

import com.quod.mappingUtils.MappingUtility;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main Class for reposition model
 */
public class Repo {
    private String repoId;

    private String name;

    private Integer numberOfCommit = 0;

    private float averNumCommPerDay = 0f;

    private float ratioCommPerDev = 0f;

    private Integer numberOfContri = 0;

    private Integer totalOfCloseIssue = 0;

    private long totalDurIssue = 0L;

    private float averIssueOpenDur = 0f;

    private float averPullEvenOpenDur = 0f;

    private Date startTime;

    private Date endTime;

    private List<Event> issueEventList;

    private List<Event> pushEventList;

    private List<Event> pullEventList;

    private List<Event> otherEventList;

    //commit push score
    private float commitScore = 0f;
    //number of project contributor score
    private float contributorScore = 0f;
    // average issue open duration score
    private float issueDurScore = 0f;
    // request pull duration score
    private float pullEvenDurScore = 0f;
    // average commit per date score
    private float averageCommPerDayScore = 0f;
    // ration commit per dev score
    private float ratioCommScore = 0f;
    private float totalScore = 0f;

    /**
     * Init repo class instance
     * @param repoId repo id
     * @param name repo name
     * @param pushEventList push event list
     * @param issueEventList issue event list
     * @param pullEventList pull event list
     * @param otherEventList other event list
     * @param repoStartTime report starttime
     * @param repoEndTime repo endtime
     */
    public Repo(String repoId, String name, List<Event> pushEventList, List<Event> issueEventList,
                List<Event> pullEventList, List<Event> otherEventList, Date repoStartTime, Date repoEndTime) {
        this.repoId = repoId;
        this.name = name;
        this.pushEventList = pushEventList;
        this.issueEventList = issueEventList;
        this.otherEventList = otherEventList;
        this.pullEventList = pullEventList;
        this.startTime = repoStartTime;
        this.endTime = repoEndTime;
        this.initIssueEventData();
        this.initPushEventData();
        this.initPullEventData();
    }

    /**
     * init data related to issue event
     */
    private void initIssueEventData() {
        if (!CollectionUtils.isEmpty(this.issueEventList)) {
            Integer totalOfCloseIssue = 0;
            long issueDurTotal = 0L;
            for (Event even : this.issueEventList) {
                if ("closed".equalsIgnoreCase(even.getEvenStatus())) {
                    long eventDuration = MappingUtility.getDateDiff(even.getCloseTime(), even.getCreateTime(), TimeUnit.SECONDS);
                    issueDurTotal += eventDuration;
                    totalOfCloseIssue += 1;
                }
            }
            //cal average duration of issue open
            float averIssueOpenDur = 0f;
            if (totalOfCloseIssue > 0) {
                averIssueOpenDur = (float) issueDurTotal / totalOfCloseIssue;
            }
            this.setAverIssueOpenDur(averIssueOpenDur);
            this.setTotalDurIssue(issueDurTotal);
            this.setTotalDurIssue(totalOfCloseIssue);
        }
    }

    /**
     * init data related to pull request event
     */
    private void initPullEventData() {
        if (!CollectionUtils.isEmpty(this.pullEventList)) {
            int totalOfPullRequest = 0;
            long pullRequestDurTotal = 0L;
            // calculate average time pull request get merge
            for (Event event : this.pullEventList) {
                if ("closed".equalsIgnoreCase(event.getEvenStatus())) {
                    long eventDuration = MappingUtility.getDateDiff(event.getCloseTime(), event.getCreateTime(), TimeUnit.SECONDS);
                    pullRequestDurTotal += eventDuration;
                    totalOfPullRequest += 1;
                }
            }

            float averPullRequestDur = 0f;
            if (totalOfPullRequest > 0) {
                averPullRequestDur = (float) pullRequestDurTotal / totalOfPullRequest;
            }

            this.setAverPullEvenOpenDur(averPullRequestDur);
        }
    }

    /**
     * int date related to push request event
     */
    private void initPushEventData() {
        if (!CollectionUtils.isEmpty(this.pushEventList)) {
            List<String> contributorsIds = new ArrayList<>();
            Integer numCommits = 0;
            Integer numContributors = 0;

            for (Event even : this.pushEventList) {
                numCommits += 1;
                if (!contributorsIds.contains(even.getActorId())) {
                    contributorsIds.add(even.getActorId());
                }
            }
            //cal average time commit per date
            if (this.startTime != null && this.endTime != null) {
                long dayNum = MappingUtility.getDateDiff(this.startTime, this.endTime, TimeUnit.DAYS);
                //avoid exception
                if (dayNum == 0L) {
                    dayNum = 1L;
                }
                float averageCommPerDay = (float) numCommits / dayNum;
                this.setAverNumCommPerDay(averageCommPerDay);
            }
            //cal ratio commit per dev
            numContributors = contributorsIds.size();

            if (numContributors != 0) {
                float commit2DevRatio = (float) numCommits / numContributors;
                this.setRatioCommPerDev(commit2DevRatio);
            }
            this.setNumberOfCommit(numCommits);
            this.setNumberOfContri(numContributors);

        }
    }

    /*
    computing repo metric
    */
    public float calCommitsScore(Integer maxNumCommits) {
        try {
            float commitScore = (float) this.numberOfCommit / maxNumCommits;
            this.setCommitScore(commitScore);
            return commitScore;
        } catch (Exception ex) {
            Logger.getLogger(Repo.class.getName()).log(Level.SEVERE, null, ex);
            this.setCommitScore(0f);
        }
        return 0f;
    }

    /**
     * calculate score base on number of contributor
     * @param maxNumContributors
     * @return
     */
    public float calContributorScore(Integer maxNumContributors) {
        try {
            float contributorScore = (float) this.numberOfContri / maxNumContributors;
            this.setContributorScore(contributorScore);
            return contributorScore;
        } catch (Exception ex) {
            Logger.getLogger(Repo.class.getName()).log(Level.SEVERE, null, ex);
            this.setContributorScore(0f);
        }
        return 0f;
    }

    /**
     * calculate score base on issue open duration
     * @param minIssueDur
     * @return
     */
    public float calIssueOpenDurScore(float minIssueDur) {
        try {
            float issueDurScore = 0.0f;
            if (this.averIssueOpenDur != 0) {

                issueDurScore = (float) minIssueDur / this.averIssueOpenDur;
            }

            this.setIssueDurScore(issueDurScore);
            return issueDurScore;
        } catch (Exception ex) {
            Logger.getLogger(Repo.class.getName()).log(Level.SEVERE, null, ex);
            this.setIssueDurScore(0f);
        }
        return 0f;
    }

    /**
     * calculcate average pull request duration per date
     * @param minPullEventDuration
     * @return
     */
    public float calAverPullRequestDurScore(float minPullEventDuration){
        try{
            float averPullRequestDurScore = 0f;
            if (this.averPullEvenOpenDur != 0){
                averPullRequestDurScore = (float) minPullEventDuration / this.averPullEvenOpenDur;
            }
            this.setPullEvenDurScore(averPullRequestDurScore);
            return issueDurScore;
        } catch (Exception ex) {
            Logger.getLogger(Repo.class.getName()).log(Level.SEVERE, null, ex);
            this.setPullEvenDurScore(0f);
        }
        return 0f;

    }

    /**
     * calculate average commit reuqest per date
     * @param maxCommPerDate
     * @return
     */
    public float calAverCommPerDateScore(float maxCommPerDate){
        try{
            float averCommPerDateScore = (float) this.averNumCommPerDay / maxCommPerDate ;
            this.setAverageCommPerDayScore(averCommPerDateScore);
            return averCommPerDateScore;
        } catch (Exception ex) {
            System.out.println(ex);
            this.setAverageCommPerDayScore(0f);
        }
        return 0f;

    }

    /**
     * calculate score base on commit per dev ration
     * @param maxCommRatio
     * @return
     */
    public float calCommitRatioScore(float maxCommRatio){
        try {

            float commRatioScore = (float) this.ratioCommPerDev / maxCommRatio;
            this.setRatioCommScore(commRatioScore);
            return commRatioScore;
        } catch (Exception ex) {
            System.out.println(ex);
            this.setRatioCommScore(0f);
        }
        return 0f;

    }

    /**
     * cal total average score of repo
     */
    public void callTotalScore() {
        float totalScore = (float) (this.contributorScore + this.commitScore +
                this.issueDurScore + this.averageCommPerDayScore + this.pullEvenDurScore + this.ratioCommScore) / 6;
        this.setTotalScore(totalScore);
    }

    /*
    getters and setters
    */
    public String getRepoId() {
        return repoId;
    }

    public void setRepoId(String repoId) {
        this.repoId = repoId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNumberOfCommit() {
        return numberOfCommit;
    }

    public void setNumberOfCommit(Integer numberOfCommit) {
        this.numberOfCommit = numberOfCommit;
    }

    public Integer getNumberOfContri() {
        return numberOfContri;
    }

    public void setNumberOfContri(Integer numberOfContri) {
        this.numberOfContri = numberOfContri;
    }

    public Integer getTotalOfCloseIssue() {
        return totalOfCloseIssue;
    }

    public void setTotalOfCloseIssue(Integer totalOfCloseIssue) {
        this.totalOfCloseIssue = totalOfCloseIssue;
    }

    public long getTotalDurIssue() {
        return totalDurIssue;
    }

    public void setTotalDurIssue(long totalDurIssue) {
        this.totalDurIssue = totalDurIssue;
    }

    public List<Event> getOtherEventList() {
        return otherEventList;
    }

    public void setOtherEventList(List<Event> otherEventList) {
        this.otherEventList = otherEventList;
    }

    public List<Event> getIssueEventList() {
        return issueEventList;
    }

    public void setIssueEventList(List<Event> issueEventList) {
        this.issueEventList = issueEventList;
    }

    public List<Event> getPushEventList() {
        return pushEventList;
    }

    public void setPushEventList(List<Event> pushEventList) {
        this.pushEventList = pushEventList;
    }

    public float getCommitScore() {
        return commitScore;
    }

    public void setCommitScore(float commitScore) {
        this.commitScore = commitScore;
    }

    public float getContributorScore() {
        return contributorScore;
    }

    public void setContributorScore(float contributorScore) {
        this.contributorScore = contributorScore;
    }

    public float getIssueDurScore() {
        return issueDurScore;
    }

    public void setIssueDurScore(float issueDurScore) {
        this.issueDurScore = issueDurScore;
    }

    public float getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(float totalScore) {
        this.totalScore = totalScore;
    }

    public float getAverIssueOpenDur() {
        return averIssueOpenDur;
    }

    public void setAverIssueOpenDur(float averIssueOpenDur) {
        this.averIssueOpenDur = averIssueOpenDur;
    }

    public float getAverPullEvenOpenDur() {
        return averPullEvenOpenDur;
    }

    public void setAverPullEvenOpenDur(float averPullEvenOpenDur) {
        this.averPullEvenOpenDur = averPullEvenOpenDur;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public float getAverNumCommPerDay() {
        return averNumCommPerDay;
    }

    public void setAverNumCommPerDay(float averNumCommPerDay) {
        this.averNumCommPerDay = averNumCommPerDay;
    }

    public float getRatioCommPerDev() {
        return ratioCommPerDev;
    }

    public void setRatioCommPerDev(float ratioCommPerDev) {
        this.ratioCommPerDev = ratioCommPerDev;
    }

    public List<Event> getPullEventList() {
        return pullEventList;
    }

    public void setPullEventList(List<Event> pullEventList) {
        this.pullEventList = pullEventList;
    }

    public float getPullEvenDurScore() {
        return pullEvenDurScore;
    }

    public void setPullEvenDurScore(float pullEvenDurScore) {
        this.pullEvenDurScore = pullEvenDurScore;
    }

    public float getAverageCommPerDayScore() {
        return averageCommPerDayScore;
    }

    public void setAverageCommPerDayScore(float averageCommPerDayScore) {
        this.averageCommPerDayScore = averageCommPerDayScore;
    }

    public float getRatioCommScore() {
        return ratioCommScore;
    }

    public void setRatioCommScore(float ratioCommScore) {
        this.ratioCommScore = ratioCommScore;
    }
}
