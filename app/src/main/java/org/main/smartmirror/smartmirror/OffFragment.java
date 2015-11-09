package org.main.smartmirror.smartmirror;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * This fragment will be all black simulating the "Off" feature
 */

public class OffFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.off_fragment, container, false);
        super.onCreate(savedInstanceState);
        return view;
    }
}
