package org.main.smartmirror.smartmirror;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewsSectionListFragment extends Fragment {


    public NewsSectionListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.news_section_list_fragment, container, false);
        TextView nameList = (TextView) view.findViewById(R.id.news_section_list);

        String text = "";
        String[] sectionNames = getResources().getStringArray(R.array.guardian_sections);
        for(String s : sectionNames) {
            text += capitalize(s) + "\n";
        }
        nameList.setText(text);

        return view;
    }

    private String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }
}
