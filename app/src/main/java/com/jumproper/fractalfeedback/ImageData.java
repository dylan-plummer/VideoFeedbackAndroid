package com.jumproper.fractalfeedback;

/**
 * Created by jumpr on 10/16/2016.
 */

public class ImageData {
    private String user;
    private String name;
    private int votes;
    private String download;
    private long date;
    private String key;
    private String uId;

    public ImageData() {
        user="";
        name="";
        votes=0;
        download="";
        date=System.currentTimeMillis();
    }

    public ImageData(String user, String name, int votes, String download, long date, String key, String uId) {
        this.user = user;
        this.name = name;
        this.votes = votes;
        this.download = download;
        this.date=date;
        this.key=key;
        this.uId=uId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public String getDownload() {
        return download;
    }

    public void setDownload(String download) {
        this.download = download;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public void incrementVotes(){
        this.votes++;
    }
    public void decrementVotes(){
        this.votes--;
    }
}
