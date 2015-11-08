package org.main.smartmirror.smartmirror;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Fragment that displays the events in one's calendar
 */
public class CalendarFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Button btnEvents = new Button(getActivity());
        btnEvents.setText("Events Today");
        return btnEvents;
    }
}
