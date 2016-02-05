package org.main.smartmirror.smartmirror;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Fragment that handles the traffic information
 */
public class TrafficFragment extends Fragment {
    private ImageView imgTrafficIcon;
    private Preferences mPreference;
    private String mCurrentLat;
    private String mCurrentLong;
    private String mWorkLat;
    private String mWorkLong;
    private TextView txtDistance;
    private TextView txtCurrent;
    private TextView txtDelays;

    Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreference = Preferences.getInstance(getActivity());
        mCurrentLat = Double.toString(mPreference.getLatitude());
        mCurrentLong = Double.toString(mPreference.getLongitude());

        //csun
        mWorkLat = "34.2370851";
        mWorkLong = "-118.5272547";

        //somewhere in la
//        mWorkLat = "34.051144";
//        mWorkLong = "-118.2366286";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.traffic_fragment, container, false);
        imgTrafficIcon = (ImageView) view.findViewById(R.id.traffic_icon);
        txtDistance = (TextView) view.findViewById(R.id.traffic_distance);
        txtCurrent = (TextView) view.findViewById(R.id.traffic_current);
        txtDelays = (TextView) view.findViewById(R.id.traffic_delay);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        startTrafficUpdate();
    }

    /**
     * Prepares the JSON request to get the current traffic information
     */

    private void startTrafficUpdate(){
        String distanceMatrixKey = getActivity().getResources().getString(R.string.distance_matrix_api_key);
        String distanceMatrixUnit = "metric";
        if(mPreference.getWeatherUnits().equals(Preferences.ENGLISH)){
            distanceMatrixUnit = "imperial";
        }
        updateTrafficData(String.format(Constants.DISTANCE_MATRIX_API, mCurrentLat, mCurrentLong, mWorkLat, mWorkLong, distanceMatrixUnit, distanceMatrixKey));

    }

    /**
     * Renders the traffic information that we got from the successful JSON
     * request
     * @param json the JSONObject we receive upon successful request
     */
    private void renderTraffic(JSONObject json){
        try {
            JSONObject data = json.getJSONArray("rows")
                    .getJSONObject(0)
                    .getJSONArray("elements")
                    .getJSONObject(0);
            double tripTime = Double.parseDouble(splitString(data.getJSONObject("duration").getString("text")));
            double tripTimeTraffic = Double.parseDouble(splitString(data.getJSONObject("duration_in_traffic").getString("text")));
            double tripDistance = Double.parseDouble(splitString(data.getJSONObject("distance").getString("text")));
            double delay = 0.0;

            String travelFlow = "faster";
            if(tripTimeTraffic - tripTime > 0){
                delay = tripTimeTraffic-tripTime;
                travelFlow = "slower";
            } else if(tripTimeTraffic - tripTime < 0){
                delay = Math.abs(tripTimeTraffic-tripTime);
            } else {
                travelFlow = "no delay";
            }

            String units = "kilometers";
            if(mPreference.getWeatherUnits().equals(Preferences.ENGLISH)){
                units = "miles";
            }

            txtDistance.setText("Distance to work: " + tripDistance + " " + units);
            txtCurrent.setText("Current travel time: " + tripTimeTraffic + " minutes");
            txtDelays.setText("Traffic status: " + travelFlow + " by " + delay + " minutes");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Splits the string up using Space as the string delimiter and
     * returns the first string post split.
     * @param str the string we want to split up
     * @return the first string post split
     */
    private String splitString(String str){
        String[] token = str.split(" ");
        return token[0];
    }

    /**
     * Updates the traffic data upon a successful JSON fetch reques
     * @param request the JSON request that we want to fetch
     */

    private void updateTrafficData(final String request){
        new Thread(){
            public void run(){
                final JSONObject json = FetchURL.getJSON(request);
                if(json == null){
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(),
                                    "error no traffic found",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            renderTraffic(json);
                        }
                    });
                }
            }
        }.start();
    }
}
