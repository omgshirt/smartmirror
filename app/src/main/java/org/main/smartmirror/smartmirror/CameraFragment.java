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
//TODO: use this link: http://www.linux.com/learn/tutorials/726597-how-to-call-the-camera-in-android-part-2-capture-and-store-photos
public class CameraFragment extends Fragment implements TextureView.SurfaceTextureListener {
    //other camera variables
    int TAKE_PHOTO_CODE = 0;
    public static int count = 0;
    //original camera variables
    private static Camera mCamera;
    private TextureView mTextureView;

    //Second camera variables
    final int CAMERA_CAPTURE = 1;
    //captured picture uri
    private Uri picUri;
    final int PIC_CROP = 2;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PHOTO_CODE && resultCode == getActivity().RESULT_OK) {
            Log.d("CameraDemo", "Pic saved");
        }
//        Log.i("On Activity Result", "onActivityResult entered: ");
//        if (requestCode == CAMERA_CAPTURE && resultCode == getActivity().RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            ImageView img = (ImageView)getActivity().findViewById(R.id.picture);
//            img.setImageBitmap(imageBitmap);
//
//        }
//        if (resultCode == getActivity().RESULT_OK) {
//            if(requestCode == CAMERA_CAPTURE){
//                picUri = data.getData();
//                //performCrop();
//            } //user is returning from cropping the image
//            else if(requestCode == PIC_CROP){
//                //get the returned data
//                Bundle extras = data.getExtras();
//                //get the cropped bitmap
//                Bitmap thePic = extras.getParcelable("data");
//                //retrieve a reference to the ImageView
//                ImageView picView = (ImageView)getActivity().findViewById(R.id.picture);
//                //display the returned cropped image
//                picView.setImageBitmap(thePic);
//            }
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Works without view below
        View view = inflater.inflate(R.layout.camera_fragment, container, false);

        mTextureView = (TextureView) view.findViewById(R.id.cameraView);
        mTextureView.setSurfaceTextureListener(this);
        final String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/picFolderTest/";
        File newdir = new File(dir);
        newdir.mkdirs();

            Button capture = (Button) view.findViewById(R.id.takepicture);
            capture.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //takePhoto();

                    //else{
//                    mCamera.set
//                    takePhoto();
//                }
                    //Codeblock works sort of
//                mCamera.release();
//
//                // here,counter will be incremented each time,and the picture taken by camera will be stored as 1.jpg,2.jpg and likewise.
//                count++;
//                String file = dir + count + ".jpg";
//                File newfile = new File(file);
//                try {
//                    newfile.createNewFile();
//                } catch (IOException e) {
//                }
//
//                Uri outputFileUri = Uri.fromFile(newfile);
//
//                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
//
//                startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);

//                try {
//                    //use standard intent to capture an image
//                    Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    //we will handle the returned data in onActivityResult
//                    startActivityForResult(captureIntent, CAMERA_CAPTURE);
//
//                   // mCamera.startPreview();
//
//                } catch (ActivityNotFoundException anfe) {
//                    //display an error message
//                    String errorMessage = "Whoops - your device doesn't support capturing images!";
//                    Toast toast = Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT);
//                    toast.show();
//                }
                }
            });


        //getActivity().setContentView(mTextureView);
        //Line below works
        //return super.onCreateView(inflater, container, savedInstanceState);
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
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/picFolderTest/");
        //final String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/picFolderTest/";
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e("getoutputmedialfile", "Failed to create storage directory.");
                return null;
            }
        }
        String timeStamp =
                new SimpleDateFormat("yyyMMdd_HHmmss", Locale.UK).format(new Date());
        if (type == MEDIA_TYPE_IMAGE) {
            return new File(dir.getPath() + File.separator + "IMG_"
                    + timeStamp + ".jpg");
        } else {
            return null;
        }
    }

//    public void captureImage(View v) throws IOException {
//        mCamera.takePicture(null, null, jpegCallback);
//    }
//
//    public void refreshCamera() {
//        if (surfaceHolder.getSurface() == null) {
//            return;
//        }
//
//        try {
//            mCamera.stopPreview();
//        }
//
//        catch (Exception e) {
//        }
//
//        try {
//            mCamera.setPreviewDisplay(surfaceHolder);
//            mCamera.startPreview();
//        }
//        catch (Exception e) {
//        }
//    }

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
        mCamera = Camera.open(cameraId);//ERROR
        Matrix transform = new Matrix();
        Camera.Parameters param = mCamera.getParameters();
        param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
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
//        mCamera.stopPreview();
//        //mCamera.setPreviewCallback(null);
//        mCamera.release();
//        mCamera=null;
//            mCamera.stopPreview();
//            mCamera.release();
        if(this.mCamera != null){
            mCamera.stopPreview();
            // the next two lines lead to the error after switching back to the app and taking a picure
            mCamera.release();
            this.mCamera = null;
        }


        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {


    }



//    class SavePhotoTask extends AsyncTask<byte[], String, String> {
//        @Override
//        protected String doInBackground(byte[]... data) {
//            File picFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
//            if (picFile == null) {
//                //Log.e(TAG, "Error creating media file; are storage permissions correct?");
//                return null;
//            }
//
//            byte[0]photoData = data[0];
//            try {
//                FileOutputStream fos = new FileOutputStream(picFile);
//                fos.write(photoData);
//                fos.close();
//            } catch (FileNotFoundException e) {
//                //Log.e(TAG, "File not found: " + e.getMessage());
//                e.getStackTrace();
//            } catch (IOException e) {
//                // Log.e(TAG, "I/O error with file: " + e.getMessage());
//                e.getStackTrace();
//            }
//            return null;
//        }
//    }


    @Override
    public void onPause() {
//        if (mCamera != null) {
//            mCamera.stopPreview();
//            mCamera.release();
//            mCamera = null;
//            Log.d("ONPAUSE", "releaseCamera -- done");
//        }
//        mTextureView=null;
//        super.onPause();
        super.onPause();
        try {
            // release the camera immediately on pause event
            // releaseCamera();
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onResume() {
        //Camera.open();
        super.onResume();
        if (mCamera != null) {
            mTextureView = (TextureView)getActivity().findViewById(R.id.cameraView);
            mTextureView.setSurfaceTextureListener(this);

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
}
