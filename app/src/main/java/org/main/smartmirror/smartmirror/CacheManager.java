package org.main.smartmirror.smartmirror;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * CacheManager class maintains a map of data objects for the life of the application.
 *
 * Periodically a thread will check all caches. Any expired caches will trigger callbacks to
 * onCacheExpired. Changing the contents of a cache will callback to onCacheChanged.
 *
 * To receive notifications when a cache has changed or expired, use the CacheListener interface.
 */

public class CacheManager {

    private static CacheManager mCacheManager;
    private static HashMap<String, DataCache> cacheMap;
    private static HashMap<String, List<CacheListener>> mListenersMap;
    private static ScheduledFuture<?> cacheScheduler;

    public interface CacheListener {
        /**
         * Called when a cached has expired. Expected that the listener will update the cache.
         */
        void onCacheExpired(String cacheName);

        /**
         * Called when the cache has been modified. A call to onCacheExpired will usually trigger this as well.
         */
        void onCacheChanged(String cacheName);
    }

    private CacheManager() {
        Log.i(Constants.TAG, "Creating CacheManager");
        cacheMap = new HashMap<>();
        mListenersMap = new HashMap<>();
        ScheduledThreadPoolExecutor scheduler = (ScheduledThreadPoolExecutor)
                Executors.newScheduledThreadPool(1);

        final Runnable expirationChecker = new Runnable() {
            @Override
            public void run() {
                checkCacheExpiration();
            }
        };

        // Schedule checks for any expired caches.
        if (cacheScheduler == null)
            cacheScheduler = scheduler.scheduleAtFixedRate(expirationChecker, 30, 30, TimeUnit.SECONDS);
    }

    public static CacheManager getInstance() {
        if (mCacheManager == null) {
            mCacheManager = new CacheManager();
        }
        return mCacheManager;
    }

    public static void destroy() {
        mCacheManager = null;
        cacheScheduler.cancel(false);
    }


    /**
     * Add a cache object to the CacheManager. This will overwrite any object with the same name.
     *
     * @param key  item name
     * @param data data to store.
     * @param time time (in seconds) until the cache is considered expired
     */
    public void addCache(String key, Object data, int time) {
        Log.i(Constants.TAG, "CacheManager addCache :: " + key);
        cacheMap.put(key, new DataCache<>(data, time));
        notifyCacheChanged(key);
    }

    /**
     * Get data by key value.
     */
    public Object get(String key) {
        if (cacheMap.containsKey(key)) {
            return cacheMap.get(key).getData();
        }

        Log.i(Constants.TAG, "Cache null :: " + key);
        return null;
    }

    public boolean containsKey(String key) {
        return cacheMap.containsKey(key);
    }

    /**
     * Delete the cache object given by 'key'
     *
     * @param key key of cache to delete
     * @return returns TRUE if cache was deleted, FALSE on failure or key not found
     */
    public boolean deleteCache(String key) {
        if (cacheMap.containsKey(key)) {
            cacheMap.remove(key);
            Log.i(Constants.TAG, "CacheManager deleteCache :: " + key);
            return true;
        }
        return false;
    }

    /** Invalidate the cache given by key. Invalid caches still exist, but are marked for update the
     *  next time the CacheManager thread checks them.
     *
     * @param key cache to invalidate
     * @return boolean true if cache exists and was invalidated.
     */
    public boolean invalidateCache(String key) {
        return (cacheMap.containsKey(key) && cacheMap.get(key).invalidate() );
    }

    public boolean isExpired(String key) {
        return (!cacheMap.containsKey(key) || cacheMap.get(key).isExpired());
    }

    /**
     * Register to listen for updates to cacheName.
     *
     * @param cacheName   name of the cache
     * @param newListener CacheListener to call back
     */
    public void registerCacheListener(String cacheName, CacheListener newListener) {
        List<CacheListener> listeners = new ArrayList<>();
        if (mListenersMap.containsKey(cacheName)) {
            listeners = mListenersMap.get(cacheName);
        }
        // ensure one listener can't sign up twice for the same cache.
        if (!listeners.contains(newListener)) {
            //Log.i(Constants.TAG, "CacheManager adding listener :: " + newListener);
            listeners.add(newListener);
            mListenersMap.put(cacheName, listeners);
        }
    }

    /**
     * Unregister the listener for the cache given by key, if it is registered
     *
     * @param key      cache name to unregister
     * @param listener listener to unregister
     */
    public void unRegisterCacheListener(String key, CacheListener listener) {
        if (mListenersMap.containsKey(key)) {
            //Log.i(Constants.TAG, "CacheManager unregistering :: " + listener);
            mListenersMap.get(key).remove(listener);
        }
    }

    /**
     * Unregister the given listener from all cache notifications it is associated with
     *
     * @param listener listener object to unregister
     */
    public void unRegisterCacheListener(CacheListener listener) {
        if (mListenersMap.containsValue(listener)) {
            for (String key : mListenersMap.keySet()) {
                if (mListenersMap.get(key).contains(listener)) {
                    mListenersMap.get(key).remove(listener);
                }
            }
        }
    }

    /**
     * Checks all caches for expiration & notifies all attached listeners via onCacheExpired method.
     */
    public void checkCacheExpiration() {
        if (mListenersMap.isEmpty()) return;
        for (String key : mListenersMap.keySet()) {
            if (cacheMap.get(key).isExpired()) {
                for (CacheListener cl : mListenersMap.get(key)) {
                    cl.onCacheExpired(key);
                }
            }
        }
    }

    /**
     * Notify all attached listeners that the cache has been updated via onCacheChanged method.
     * Note: this is not called when a cache object is deleted.
     *
     * @param cacheName name of the updated cache.
     */
    public void notifyCacheChanged(String cacheName) {
        if (!mListenersMap.containsKey(cacheName)) return;
        for (CacheListener cl : mListenersMap.get(cacheName)) {
            cl.onCacheChanged(cacheName);
        }
    }

    private class DataCache<T> {

        private T data;
        private Date expirationTime;
        private Date creationTime;

        public DataCache() {
            data = null;
            expirationTime = new Date();
            creationTime = new Date(System.currentTimeMillis());
        }

        /**
         * @param data Data to store
         * @param time expiration (in seconds) from current time
         */
        public DataCache(T data, int time) {
            expirationTime = new Date();
            creationTime = new Date(System.currentTimeMillis());
            setData(data, time);
        }

        /**
         * @param data         Data to store
         * @param dataDuration time (in seconds) before the cache is considered expired
         */
        public void setData(T data, int dataDuration) {
            this.data = data;
            long now = System.currentTimeMillis();
            expirationTime.setTime(now + (long) dataDuration * 1000);
        }

        public T getData() {
            return data;
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
        public boolean invalidate() {
            this.data = null;
            this.creationTime = new Date(0);
            this.expirationTime = new Date(0);
            return true;
        }

        public Date getCreationTime() {
            return creationTime;
        }
    }
}
