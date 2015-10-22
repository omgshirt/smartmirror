package org.main.smartmirror.smartmirror;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * Created by Master N on 10/17/2015.
 */
public class NewsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView txtFragmentLabel = new TextView(getActivity());
        txtFragmentLabel.setText("SmartMirror News");
        txtFragmentLabel.setTextSize(15);
        return txtFragmentLabel;
    }
}
