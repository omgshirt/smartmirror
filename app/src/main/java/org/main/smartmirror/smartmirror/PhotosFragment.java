package org.main.smartmirror.smartmirror;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gdata.client.*;
import com.google.gdata.client.photos.*;
import com.google.gdata.data.*;
import com.google.gdata.data.media.*;
import com.google.gdata.data.photos.*;
import com.google.gdata.util.ServiceException;


public class PhotosFragment extends Fragment {

    ImageView mPhotoFromPicasa;
    URL albumPostUrl;
    private PicasawebService service;
    Handler mHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photos_fragment, container, false);
        mPhotoFromPicasa = (ImageView) view.findViewById(R.id.photo_from_picasa);

        //new PhotosASyncTask(service,"smartmirrortesting@gmail.com", "smartmirrort").execute();


        return view;
    }

    public void renderPhoto(JSONObject json) {

    }

    // Get picasa
    private void updatePicasa(final String query) {
        new Thread() {
            public void run() {
                final JSONObject json = FetchURL.getJSON(query);
                if (json == null) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            ((MainActivity) getActivity()).showToast(getString(R.string.photos_err),
                                    Gravity.CENTER, Toast.LENGTH_LONG);
                        }
                    });
                } else {
                    mHandler.post(new Runnable() {
                        public void run() {
                            try {;
                                Log.i("PICASA ", json.toString());
                                //renderNews(json);
                            } catch (Exception e) {Log.i("picasa ", e.toString());}

                        }
                    });
                }
            }
        }.start();
    }


}
