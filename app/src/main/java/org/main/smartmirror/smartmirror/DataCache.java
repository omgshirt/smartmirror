package org.main.smartmirror.smartmirror;

import java.util.Date;

/**
 * Created by Brian on 1/5/2016.
 *
 * Stash data of the given type within an instance of DataCache for later use.
 *
 */
public class DataCache<T> {

    private T data;
    private Date expirationTime;
    private Date creationTime;

    public DataCache() {
        data = null;
        expirationTime = new Date();
        creationTime = new Date(System.currentTimeMillis());
    }

    /**
     *
     * @param data Data to store
     * @param time expiration (in minutes) from current time
     */
    public DataCache(T data, int time) {
        expirationTime = new Date();
        creationTime = new Date(System.currentTimeMillis());
        setData(data, time);
    }

    /**
     * Sets the data to never expire
     */
    public void setData(T data) {
        this.expirationTime.setTime(Long.MAX_VALUE);
        this.data = data;
    }

    /**
     *
     * @param data Data to store
     * @param dataDuration time (in minutes) before the cache is considered expired
     */
    public void setData(T data, int dataDuration) {
        this.data = data;
        long now = System.currentTimeMillis();
        expirationTime.setTime(now + (long)dataDuration * 60 * 1000);
    }

    public T getData() {
        return  data;
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

    /**
     * Invalidate resets the data
     */
    public void invalidate() {
        this.data = null;
        this.creationTime = new Date(0);
        this.expirationTime = new Date(0);
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }
}
