package com.quod.postprocess;

import java.util.Date;

public class Event {
    private String evenId;

    private String actorAccount;

    private String actorId;

    private Boolean isPublic;

    private String evenType;

    private String evenStatus;

    private Date createTime;

    private Date updateTime;

    private Date closeTime;

    private String repoName;

    private String repoId;

    public Event(String evenId, String actorAccount, String actorId, Boolean isPublic,
                 String evenType, String evenStatus, String repoName, String repoId, Date createTime, Date updateTime, Date closeTime) {
        this.evenId = evenId;
        this.actorAccount = actorAccount;
        this.actorId = actorId;
        this.isPublic = isPublic;
        this.evenType = evenType;
        this.repoName = repoName;
        this.repoId = repoId;
        this.evenStatus = evenStatus;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.closeTime = closeTime;
    }

    public String getEvenId() {
        return evenId;
    }

    public void setEvenId(String evenId) {
        this.evenId = evenId;
    }

    public String getActorAccount() {
        return actorAccount;
    }

    public void setActorAccount(String actorAccount) {
        this.actorAccount = actorAccount;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public Boolean getPublic() {
        return isPublic;
    }

    public void setPublic(Boolean aPublic) {
        isPublic = aPublic;
    }

    public String getEvenType() {
        return evenType;
    }

    public void setEvenType(String evenType) {
        this.evenType = evenType;
    }

    public String getEvenStatus() {
        return evenStatus;
    }

    public void setEvenStatus(String evenStatus) {
        this.evenStatus = evenStatus;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Date closeTime) {
        this.closeTime = closeTime;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getRepoId() {
        return this.repoId;
    }

    public void setRepoId(String repoId) {
        this.repoId = repoId;
    }
}
