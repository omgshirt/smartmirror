package org.main.smartmirror.smartmirror;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;


public class PhotosFragment extends Fragment {

    public static ImageView mPhotoFromPicasa;
    public static ArrayList<Uri> mImageUrlList = new ArrayList<Uri>();
    public static ArrayList<String> mAlbumIdList = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photos_fragment, container, false);
        mPhotoFromPicasa = (ImageView) view.findViewById(R.id.photo_from_picasa);

        new PhotosASyncTask(getActivity()).execute();
        return view;
    }

}
