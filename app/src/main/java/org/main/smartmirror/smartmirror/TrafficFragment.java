package org.main.smartmirror.smartmirror;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Fragment that handles the traffic information
 */
public class TrafficFragment extends Fragment implements CacheManager.CacheListener {
    private Preferences mPreference;
    private ViewGroup mTrafficLayout;
    private String mCurrentLat;
    private String mCurrentLong;
    private String mWorkLat;
    private String mWorkLong;
    private TextView txtDestination;
    private TextView txtTravelTime;

    private Handler mHandler = new Handler();
    private CacheManager mCacheManager = null;
    // time in seconds before treffic data expires 10 minute default
    private final int DATA_UPDATE_FREQUENCY = 600;
    public static final String TRAFFIC_CACHE = "traffic cache";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCacheManager = CacheManager.getInstance();
        mPreference = Preferences.getInstance(getActivity());
        mCurrentLat = Double.toString(mPreference.getLatitude());
        mCurrentLong = Double.toString(mPreference.getLongitude());
        mWorkLat = Double.toString(mPreference.getWorkLatitude());
        mWorkLong = Double.toString(mPreference.getWorkLongitude());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.traffic_fragment, container, false);
        mTrafficLayout = (LinearLayout) view.findViewById(R.id.traffic_layout);
        txtDestination = (TextView) view.findViewById(R.id.traffic_destination);
        txtTravelTime = (TextView) view.findViewById(R.id.traffic_delay);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mPreference.isWorkAddressSet()) {
            mTrafficLayout.setVisibility(View.VISIBLE);
            // Check for any cached traffic data.
            // If a cache exists, render it to the view.
            // Update the cache if it has expired.
            if (!mCacheManager.containsKey(TRAFFIC_CACHE)) {
                startTrafficUpdate();
            } else {
                renderTraffic((JSONObject) mCacheManager.get(TRAFFIC_CACHE));
                if (mCacheManager.isExpired(TRAFFIC_CACHE)) {
                    Log.i(Constants.TAG, "TrafficCache expired. Refreshing...");
                    startTrafficUpdate();
                }
            }
        } else {
            // traffic wasn't set so let's hide it.
            // TODO think about this more.
            mCacheManager.deleteCache(TRAFFIC_CACHE);
            hideTraffic();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mCacheManager.registerCacheListener(TRAFFIC_CACHE, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mCacheManager.unRegisterCacheListener(this);
    }

    /**
     * Prepares the JSON request to get the current traffic information
     */
    private void startTrafficUpdate() {
        String distanceMatrixKey = getActivity().getResources().getString(R.string.distance_matrix_api_key);
        String distanceMatrixUnit = "metric";
        if (mPreference.getWeatherUnits().equals(Preferences.ENGLISH)) {
            distanceMatrixUnit = "imperial";
        }

        if (mWorkLat.isEmpty() && mWorkLong.isEmpty()) {
            hideTraffic();
        } else {
            updateTrafficData(String.format(Constants.DISTANCE_MATRIX_API, mCurrentLat, mCurrentLong, mWorkLat, mWorkLong, distanceMatrixUnit, distanceMatrixKey));
        }
    }

    private void hideTraffic() {
        mTrafficLayout.setVisibility(View.GONE);
    }

    private void showTraffic() {
        mTrafficLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Renders the traffic information that we got from the successful JSON
     * request
     *
     * @param json the JSONObject we receive upon successful request
     */
    private void renderTraffic(JSONObject json) {
        try {
            JSONObject data = json.getJSONArray("rows")
                    .getJSONObject(0)
                    .getJSONArray("elements")
                    .getJSONObject(0);
            String tripTime = data.getJSONObject("duration").getString("text");

            String units = "km";
            if (mPreference.getWeatherUnits().equals(Preferences.ENGLISH)) {
                units = "mi";
            }

            txtDestination.setText(mPreference.getWorkLocation());
            txtTravelTime.setText(tripTime);

            // make sure traffic is now visible!
            showTraffic();
        } catch (NullPointerException npe) {
          npe.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Splits the string up using Space as the string delimiter and
     * returns the first string post split.
     *
     * @param str the string we want to split up
     * @return the first string post split
     */
    private String splitString(String str) {
        String[] token = str.split(" ");
        return token[0];
    }

    /**
     * Updates the traffic data upon a successful JSON fetch reques
     *
     * @param request the JSON request that we want to fetch
     */
    private void updateTrafficData(final String request) {
        new Thread() {
            public void run() {
                final JSONObject json = FetchURL.getJSON(request);
                if (json == null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ((MainActivity) getActivity()).showToast("error no traffic found",
                                    Gravity.CENTER, Toast.LENGTH_LONG);
                            hideTraffic();
                            updateTrafficCache(null);
                        }
                    });
                } else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateTrafficCache(json);
                            renderTraffic(json);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * Handles the update to the traffic cache
     *
     * @param data the data to cache
     */
    private void updateTrafficCache(JSONObject data) {
        mCacheManager.addCache(TRAFFIC_CACHE, data, DATA_UPDATE_FREQUENCY);
    }

    // refresh the traffic when the cache has expired.
    @Override
    public void onCacheExpired(String cacheName) {
        if (cacheName.equals(TRAFFIC_CACHE)) startTrafficUpdate();
    }

    // we don't need to listen to the cache changing
    // since we're handling the refresh in onCacheExpired
    @Override
    public void onCacheChanged(String cacheName) {

    }
}
