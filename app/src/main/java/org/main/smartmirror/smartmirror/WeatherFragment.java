package org.main.smartmirror.smartmirror;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * Created by Master N on 10/20/2015.
 */
public class WeatherFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView txtFragmentLabel = new TextView(getActivity());
        txtFragmentLabel.setText("SmartMirror Weather");
        txtFragmentLabel.setTextSize(25);
        return txtFragmentLabel;
    }
}
