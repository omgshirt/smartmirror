package org.main.smartmirror.smartmirror;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Jesus on 11/28/2015.
 */
public class MakeupFragment extends Fragment

{
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);


    }


@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.makeup_fragment, container, false);

        return view;
    }


}