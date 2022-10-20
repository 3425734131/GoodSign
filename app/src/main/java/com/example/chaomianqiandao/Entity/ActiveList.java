package com.example.chaomianqiandao.Entity;

import java.util.Date;

public class ActiveList {
    private String nameTwo;
    private int isLook;
    private String picUrl;
    private int activeType;
    private String nameOne;
    private String id;
    private int status;
    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNameTwo() {
        return nameTwo;
    }

    public void setNameTwo(String nameTwo) {
        this.nameTwo = nameTwo;
    }

    public void setIsLook(int isLook) {
        this.isLook = isLook;
    }
    public int getIsLook() {
        return isLook;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }
    public String getPicUrl() {
        return picUrl;
    }


    public void setActiveType(int activeType) {
        this.activeType = activeType;
    }
    public int getActiveType() {
        return activeType;
    }

    public void setNameOne(String nameOne) {
        this.nameOne = nameOne;
    }
    public String getNameOne() {
        return nameOne;
    }


    public void setStatus(int status) {
        this.status = status;
    }
    public int getStatus() {
        return status;
    }


}