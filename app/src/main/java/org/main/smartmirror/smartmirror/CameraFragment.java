package org.main.smartmirror.smartmirror;

/**
 * Created by Master N on 11/6/2015.
 */

//FROM: http://stackoverflow.com/questions/20878232/android-using-settransform-for-flipping-the-live-textureview/20883662#20883662
//Additional help from: http://developer.android.com/reference/android/view/TextureView.html
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Handler;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
//use this link: http://www.linux.com/learn/tutorials/726597-how-to-call-the-camera-in-android-part-2-capture-and-store-photos
public class CameraFragment extends Fragment implements TextureView.SurfaceTextureListener {

    int TAKE_PHOTO_CODE = 0;
    private static Camera mCamera;
    private TextureView mTextureView;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.camera_fragment, container, false);

        mTextureView = (TextureView) view.findViewById(R.id.cameraView);
        mTextureView.setSurfaceTextureListener(this);
         final String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/smartMirrorPics/";
         final File newdir = new File(dir);
        if(!newdir.exists()) {
            newdir.mkdirs();
            Log.i("Checking Dir: ", " if directory doesnt exist and create directory if so");
        }else {
            Log.i("Checking Dir: ", " apparently directory exists");
            newdir.mkdirs();
        }

            final Button capture = (Button) view.findViewById(R.id.takepicture);
            capture.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    capture.setEnabled(false);
                    takePhoto();

                    //https://rajareddypolam.wordpress.com/2013/01/29/android-saving-file-to-external-storage/
                    getActivity().sendBroadcast(new Intent(
                            Intent.ACTION_MEDIA_MOUNTED,
                            Uri.parse("file://" + Environment.getExternalStorageDirectory())));
                }
            });

        return view;
    }

    protected static final int MEDIA_TYPE_IMAGE = 0;

    private void takePhoto() {
        mTextureView=null;
        Camera.PictureCallback pictureCB = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera cam) {

                File picFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (picFile == null) {
                    Log.e("CANT CREATE takePhoto", "Couldn't create media file; check storage permissions?");
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(picFile);
                    Log.e("writing data?", "creating file?");
                    fos.write(data);
                    fos.close();

                } catch (FileNotFoundException e) {
                    Log.e("NOT FOUND takePhoto", "File not found: " + e.getMessage());
                    e.getStackTrace();
                } catch (IOException e) {
                    Log.e("IOERROR takePhoto", "I/O error writing file: " + e.getMessage());
                    e.getStackTrace();
                }
            }
        };
        mCamera.takePicture(null, null, pictureCB);
        mCamera.startPreview();
    }

    private File getOutputMediaFile(int type) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/smartMirrorPics/");
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e("getoutputmedialfile", "Failed to create storage directory.");
                return null;
            }
        }
        String timeStamp =
                new SimpleDateFormat("yyyMMdd_HHmmss", Locale.UK).format(new Date());
        if (type == MEDIA_TYPE_IMAGE) {
            Log.i("OUTPUT FILE", "TESTING PICTURE");
            final Button cap = (Button) getActivity().findViewById(R.id.takepicture);
            cap.setEnabled(true);
            return new File(dir.getPath() + File.separator + "IMG_"
                    + timeStamp + ".jpg");

        } else {
            return null;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        //int result = 0;
        int cameraId = 0;
        Camera.CameraInfo info = new Camera.CameraInfo();

        for (cameraId = 0; cameraId < Camera.getNumberOfCameras(); cameraId++) {
            Camera.getCameraInfo(1, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
                break;
        }

        mCamera = Camera.open(cameraId);

        Matrix transform = new Matrix();
        Camera.Parameters param = mCamera.getParameters();
        param.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        Camera.Size previewSize = mCamera.getParameters().getPreviewSize();

        int rotation = getActivity().getWindowManager().getDefaultDisplay()
                .getRotation();

        // Log.i("onSurfaceTextureAvailable", " CameraOrientation(" + cameraId + ")" + info.orientation + " " + previewSize.width + "x" + previewSize.height + " Rotation=" + rotation);

        switch (rotation) {
            case Surface.ROTATION_0:
                mCamera.setDisplayOrientation(90);
                mTextureView.setLayoutParams(new FrameLayout.LayoutParams(
                        previewSize.height, previewSize.width, Gravity.CENTER));
                transform.setScale(-1, 1, previewSize.height / 2, 0);
                break;

            case Surface.ROTATION_90:
                mCamera.setDisplayOrientation(0);
                mTextureView.setLayoutParams(new FrameLayout.LayoutParams(
                        previewSize.width, previewSize.height, Gravity.CENTER));
                transform.setScale(-1, 1, previewSize.width / 2, 0);
                break;

            case Surface.ROTATION_180:
                mCamera.setDisplayOrientation(270);
                mTextureView.setLayoutParams(new FrameLayout.LayoutParams(
                        previewSize.height, previewSize.width, Gravity.CENTER));
                transform.setScale(-1, 1, previewSize.height / 2, 0);
                break;

            case Surface.ROTATION_270:
                mCamera.setDisplayOrientation(180);
                mTextureView.setLayoutParams(new FrameLayout.LayoutParams(
                        previewSize.width, previewSize.height, Gravity.CENTER));
                transform.setScale(-1, 1, previewSize.width / 2, 0);
                break;
        }
        try {
            mCamera.setPreviewTexture(surface);
        } catch (IOException t) {
        }
        mTextureView.setTransform(null);//"null" used to be "transform". Use transform only for front-facing camera.
        mCamera.startPreview();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Ignored, the Camera does all the work for us
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if(this.mCamera != null){
            mCamera.stopPreview();
            mCamera.release();
            this.mCamera = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            mCamera.setPreviewCallback(null);
            mTextureView=null;
            mTextureView.setSurfaceTextureListener(null);
            mCamera.release();
            mCamera = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCamera != null) {
            mTextureView = (TextureView)getActivity().findViewById(R.id.cameraView);
            mTextureView.setSurfaceTextureListener(this);
            //mCamera.open();
            mCamera.startPreview();
            Log.d("ONRESUME", "openCamera -- done");
        }
        mTextureView = (TextureView)getActivity().findViewById(R.id.cameraView);
        mTextureView.setSurfaceTextureListener(this);
        Log.d("ONRESUME2", "openCamera22222 -- done");
    }

//    @Override
//    public void onDestroy() {
//        if (mCamera != null) {
//            mCamera.release();
//        }
//        super.onDestroy();
//    }

    //    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        Log.i("In ACTRESULT: ", "TESTING SAVE");
//        if (requestCode == TAKE_PHOTO_CODE && resultCode == getActivity().RESULT_OK) {
//            Log.d("CameraDemo", "Pic saved");
//                Bundle extras = data.getExtras();
//                //get the cropped bitmap
//                Bitmap thePic = extras.getParcelable("data");
//                //retrieve a reference to the ImageView
//                ImageView picView = (ImageView)getActivity().findViewById(R.id.picture);
//                //display the returned cropped image
//                picView.setImageBitmap(thePic);
//        }
//    }
}
