package org.main.smartmirror.smartmirror;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Jesus on 11/5/2015.
 */
public class HelperFragment extends Fragment



{
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Resources res = getResources();
        String[] help_array = res.getStringArray(R.array.help_array);
        String helper_string= stringSpace(help_array);

        TextView txtFragmentLabel = new TextView(getActivity());

        txtFragmentLabel.setText(helper_string);
        txtFragmentLabel.setTextSize(15);




        return txtFragmentLabel;
    }

    public String stringSpace(String[] string)
    {
        String str="";
        for(int i=0;i<string.length;i++)
        {
            str+=string[i]+"\n";
        }
        return str;
   }

}
