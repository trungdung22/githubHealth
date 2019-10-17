package com.quod.postprocess;

import com.quod.dao.Repo;

import java.util.List;

public class HealthMetric {

    /**
     * @param repoList list of repository objects
     */
    public static void computeMetric(List<Repo> repoList) {
        Integer maxNumContributors = 0;
        Integer maxNumCommits = 0;
        float minIssueDuration = Float.MAX_VALUE;
        float minPullEventDuration = Float.MAX_VALUE;
        float maxCommPerDate = 0;
        float maxCommRatio = 0;
        for (Repo repo : repoList) {
            float averageIssueDur = repo.getAverIssueOpenDur();
            float averagePullDur = repo.getAverPullEvenOpenDur();

            if (averageIssueDur <= minIssueDuration && averageIssueDur != 0) {
                minIssueDuration = averageIssueDur;
            }

            if (averagePullDur <= minPullEventDuration && averagePullDur != 0) {
                minPullEventDuration = averagePullDur;
            }

            Integer numCommits = repo.getNumberOfCommit();
            Integer numContributors = repo.getNumberOfContri();
            float commPerDate = repo.getAverNumCommPerDay();
            float commRatio = repo.getRatioCommPerDev();

            if (commPerDate >= maxCommPerDate) {
                maxCommPerDate = commPerDate;
            }

            if (commRatio >= maxCommRatio) {
                maxCommRatio = commRatio;
            }

            if (numCommits >= maxNumCommits) {
                maxNumCommits = numCommits;
            }

            if (numContributors >= maxNumContributors) {
                maxNumContributors = numContributors;
            }
        }

        postProcessHealthMetric(repoList, maxNumCommits, maxNumContributors, minIssueDuration, minPullEventDuration,
                maxCommPerDate, maxCommRatio);
    }

    /**
     * COMPUATE AND NORMALIZA METRIC HEALTH SCORE INDICATOR
     *
     * @param repoList
     * @param maxNumCommits
     * @param maxNumContributors
     * @param minIssueDuration
     * @param minPullEventDuration
     * @param maxCommPerDate
     * @param maxCommRatio
     */
    private static void postProcessHealthMetric(List<Repo> repoList, Integer maxNumCommits,
                                                Integer maxNumContributors, float minIssueDuration,
                                                float minPullEventDuration,
                                                float maxCommPerDate, float maxCommRatio) {
        float maxIssueDurScore = 1L;
        float maxCommitScore = 1L;
        float maxContributorScore = 1L;
        float maxpullEventDurScore = 1L;
        float maxcommPerDateScore = 1L;
        float maxcommRatio = 1L;
        for (Repo repo : repoList) {

            float commitScore = repo.calCommitsScore(maxNumCommits);
            float contributorScore = repo.calContributorScore(maxNumContributors);
            float issueDurScore = repo.calIssueOpenDurScore(minIssueDuration);
            float pullEventDurScore = repo.calAverPullRequestDurScore(minPullEventDuration);
            float commPerDateScore = repo.calAverCommPerDateScore(maxCommPerDate);
            float commRatio = repo.calCommitRatioScore(maxCommRatio);

            if (commitScore >= maxCommitScore) {
                maxCommitScore = commitScore;
            }

            if (contributorScore >= maxContributorScore) {
                maxContributorScore = contributorScore;
            }

            if (issueDurScore >= maxIssueDurScore) {
                maxIssueDurScore = issueDurScore;
            }

            if (pullEventDurScore >= maxpullEventDurScore) {
                maxpullEventDurScore = pullEventDurScore;
            }

            if (commRatio >= maxcommRatio) {
                maxcommRatio = commRatio;
            }

            if (commPerDateScore >= maxcommPerDateScore) {
                maxcommPerDateScore = commPerDateScore;
            }
        }

        // normalization step
        for (Repo repo : repoList) {
            float commitScore = repo.getCommitScore() / maxCommitScore;
            float contributorScore = repo.getContributorScore() / maxContributorScore;
            float issueDurScore = repo.getIssueDurScore() / maxIssueDurScore;
            float pullEventDurScore = repo.getPullEvenDurScore() / maxpullEventDurScore;
            float commRatio = repo.getRatioCommScore() / maxcommRatio;
            float commPerDateScore = repo.getAverageCommPerDayScore() / maxcommPerDateScore;
            repo.setIssueDurScore(issueDurScore);
            repo.setContributorScore(contributorScore);
            repo.setCommitScore(commitScore);
            repo.setPullEvenDurScore(pullEventDurScore);
            repo.setRatioCommScore(commRatio);
            repo.setAverageCommPerDayScore(commPerDateScore);
            repo.callTotalScore();
        }
    }
}
