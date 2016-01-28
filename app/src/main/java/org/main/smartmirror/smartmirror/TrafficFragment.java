package org.main.smartmirror.smartmirror;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Fragment that handles the traffic information
 */
public class TrafficFragment extends Fragment {
    private float mZoom;
    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private Preferences mPreferance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferance = Preferences.getInstance(getActivity());
        mZoom = 14.0f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.traffic_fragment, container, false);
        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();// needed to get the map to display immediately
        mGoogleMap = mMapView.getMap();
        setUpMap();
        return view;
    }

    private void setUpMap(){
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        LatLng currentPosition = new LatLng(mPreferance.getLatitude(),mPreferance.getLongitude());
        LatLng workPosition = new LatLng(34.2415936,-118.5286617);
        mGoogleMap.addMarker(new MarkerOptions().position(currentPosition).title("You're here!"));
        mGoogleMap.addMarker(new MarkerOptions().position(workPosition).title("Work"));
        mGoogleMap.setTrafficEnabled(true);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, mZoom));
    }
}
