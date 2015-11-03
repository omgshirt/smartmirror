package org.main.smartmirror.smartmirror;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Master N on 10/17/2015.
 */
public class CalendarFragment extends Fragment {
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Intent intent = new Intent(getActivity(), CalendarActivity.class);
        startActivity(intent);
    }


//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        Button btnEvents = new Button(getActivity());
//        btnEvents.setText("Events Today");
//        Intent intent = new Intent(getActivity(), CalendarActivity.class);
//        startActivity(intent);
//        return btnEvents;
//    }

}
