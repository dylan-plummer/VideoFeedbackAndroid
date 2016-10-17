package com.jumproper.videofeedback;

import android.net.Uri;

/**
 * Created by jumpr on 10/16/2016.
 */

public class ImageData {
    private String user;
    private String name;
    private int votes;
    private String download;

    public ImageData() {
        user="";
        name="";
        votes=0;
        download="";
    }

    public ImageData(String user, String name, int votes, String download) {
        this.user = user;
        this.name = name;
        this.votes = votes;
        this.download = download;
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
}
