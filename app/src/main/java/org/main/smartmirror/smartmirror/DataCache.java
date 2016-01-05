package org.main.smartmirror.smartmirror;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Brian on 1/5/2016.
 *
 * Stash JSONObjects within an instance of DataCache for later use.
 *
 */
public class DataCache {

    private JSONObject data;
    private Date expirationTime;
    private Date creationTime;

    public DataCache() {
        data = null;
        expirationTime = new Date();
        creationTime = new Date(System.currentTimeMillis());
    }

    /**
     *
     * @param data JSONObject to store
     * @param time expiration time in minutes
     */
    public DataCache(JSONObject data, int time) {
        expirationTime = new Date();
        creationTime = new Date(System.currentTimeMillis());
        setData(data, time);
    }

    public Date getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }

    public boolean isExpired() {
        long current = System.currentTimeMillis();
        return (current > expirationTime.getTime());
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    /**
     *
     * @param data JSON data to store
     * @param dataDuration the time before the cache is expired, in minutes
     */
    public void setData(JSONObject data, int dataDuration) {
        this.data = data;
        long now = System.currentTimeMillis();
        expirationTime.setTime(now + (long)dataDuration * 60 * 1000);
    }

    public JSONObject getData() {
        return  data;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }
}
