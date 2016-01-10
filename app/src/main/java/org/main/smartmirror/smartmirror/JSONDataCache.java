package org.main.smartmirror.smartmirror;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Brian on 1/5/2016.
 *
 * Stash JSONObjects within an instance of JSONDataCache for later use.
 *
 */
public class JSONDataCache {

    private JSONObject data;
    private Date expirationTime;
    private Date creationTime;
    private Boolean valid;

    public JSONDataCache() {
        data = null;
        valid = true;
        expirationTime = new Date();
        creationTime = new Date(System.currentTimeMillis());
    }

    /**
     *
     * @param data JSONObject to store
     * @param time expiration time in minutes
     */
    public JSONDataCache(JSONObject data, int time) {
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
     * @param dataDuration time (in minutes) before the cache is considered expired
     */
    public void setData(JSONObject data, int dataDuration) {
        this.data = data;
        long now = System.currentTimeMillis();
        expirationTime.setTime(now + (long)dataDuration * 60 * 1000);
    }

    public JSONObject getData() {
        return  data;
    }

    /**
     * Calling invalidate sets the data to be considered expired.
     */
    public void invalidate() {
        this.expirationTime = new Date(0);
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }
}
