package org.main.smartmirror.smartmirror;

/**
 * Created by Master N on 11/6/2015.
 */

//FROM: http://stackoverflow.com/questions/20878232/android-using-settransform-for-flipping-the-live-textureview/20883662#20883662
//Additional help from: http://developer.android.com/reference/android/view/TextureView.html
import java.io.IOException;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

public class CameraFragment extends Fragment implements TextureView.SurfaceTextureListener{

    private Camera mCamera;
    private TextureView mTextureView;

//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        mTextureView = new TextureView(getActivity());
//        mTextureView.setSurfaceTextureListener(this);
//
//        getActivity().setContentView(mTextureView);
//    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Works without view below
        View view = inflater.inflate(R.layout.camera_fragment, container, false);
//        TextView cameraText = (TextView)view.findViewById(R.id.textCamera);
//         cameraText.setText("Testing Camera");
//        mTextureView = new TextureView(getActivity());
//        mTextureView.setSurfaceTextureListener(this);
        mTextureView = (TextureView)view.findViewById(R.id.cameraView);
        mTextureView.setSurfaceTextureListener(this);


        //getActivity().setContentView(mTextureView);
        //Line below works
        //return super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        int result = 0;
        int cameraId = 0;
        Camera.CameraInfo info = new Camera.CameraInfo();

        for (cameraId = 0; cameraId < Camera.getNumberOfCameras(); cameraId++) {
            Camera.getCameraInfo(1, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
                break;
        }
        mCamera = Camera.open(cameraId);
        Matrix transform = new Matrix();

        Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
        int rotation = getActivity().getWindowManager().getDefaultDisplay()
                .getRotation();

       // Log.i("onSurfaceTextureAvailable", " CameraOrientation(" + cameraId + ")" + info.orientation + " " + previewSize.width + "x" + previewSize.height + " Rotation=" + rotation);

        switch (rotation) {
            case Surface.ROTATION_0:
                mCamera.setDisplayOrientation(90);
                mTextureView.setLayoutParams(new FrameLayout.LayoutParams(
                        previewSize.height, previewSize.width, Gravity.CENTER));
                transform.setScale(-1, 1, previewSize.height/2, 0);
                break;

            case Surface.ROTATION_90:
                mCamera.setDisplayOrientation(0);
                mTextureView.setLayoutParams(new FrameLayout.LayoutParams(
                        previewSize.width, previewSize.height, Gravity.CENTER));
                transform.setScale(-1, 1, previewSize.width/2, 0);
                break;

            case Surface.ROTATION_180:
                mCamera.setDisplayOrientation(270);
                mTextureView.setLayoutParams(new FrameLayout.LayoutParams(
                        previewSize.height, previewSize.width, Gravity.CENTER));
                transform.setScale(-1, 1, previewSize.height/2, 0);
                break;

            case Surface.ROTATION_270:
                mCamera.setDisplayOrientation(180);
                mTextureView.setLayoutParams(new FrameLayout.LayoutParams(
                        previewSize.width, previewSize.height, Gravity.CENTER));
                transform.setScale(-1, 1, previewSize.width/2, 0);
                break;
        }


        try {
            mCamera.setPreviewTexture(surface);
        } catch (IOException t) {
        }

        mTextureView.setTransform(null);//"null" used to be "transform". Use transform only for front-facing camera.
        //Log.i("onSurfaceTextureAvailable", "Transform: " + transform.toString());

        mCamera.startPreview();
        //mCamera.autoFocus(cb);

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Ignored, the Camera does all the work for us
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mCamera.stopPreview();
        mCamera.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Update your view here!
    }



}
